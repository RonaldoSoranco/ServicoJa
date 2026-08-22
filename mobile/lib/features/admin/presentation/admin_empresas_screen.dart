import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/estado_erro.dart';
import '../../../core/widgets/estado_vazio.dart';
import '../../empresas/models/empresa.dart';
import '../data/admin_repository.dart';
import 'botao_sair_admin.dart';

/// Tela de aprovacao de empresas (Fase 1.5).
///
/// A API nao tem filtro de aprovacao no servidor: buscamos com `tamanho=50`
/// (sem paginacao real) e filtramos `aprovada == false` no cliente quando o
/// toggle "Somente pendentes" estiver ativo. Empresas cadastradas alem das
/// primeiras 50 nao aparecem aqui - limitacao documentada no plano.
class AdminEmpresasScreen extends StatefulWidget {
  const AdminEmpresasScreen({super.key});

  @override
  State<AdminEmpresasScreen> createState() => _AdminEmpresasScreenState();
}

class _AdminEmpresasScreenState extends State<AdminEmpresasScreen> {
  final _repositorio = AdminRepository();
  bool _carregando = true;
  String? _erro;
  List<Empresa> _empresas = [];
  bool _somentePendentes = true;
  final Set<int> _processando = {};

  @override
  void initState() {
    super.initState();
    _carregar();
  }

  Future<void> _carregar() async {
    setState(() {
      _carregando = true;
      _erro = null;
    });
    try {
      final pagina = await _repositorio.listarEmpresas(pagina: 0, tamanho: 50);
      if (mounted) setState(() => _empresas = pagina.conteudo);
    } on ApiException catch (e) {
      if (mounted) setState(() => _erro = e.toString());
    } catch (_) {
      if (mounted) setState(() => _erro = 'Ocorreu um erro inesperado. Tente novamente.');
    } finally {
      if (mounted) setState(() => _carregando = false);
    }
  }

  Future<void> _definirAprovacao(Empresa empresa, bool aprovada) async {
    setState(() => _processando.add(empresa.id));
    try {
      final atualizada = await _repositorio.definirAprovacao(empresa.id, aprovada);
      if (mounted) {
        setState(() {
          final indice = _empresas.indexWhere((e) => e.id == empresa.id);
          if (indice != -1) _empresas[indice] = atualizada;
        });
      }
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
    } finally {
      if (mounted) setState(() => _processando.remove(empresa.id));
    }
  }

  @override
  Widget build(BuildContext context) {
    final listaFiltrada = _somentePendentes ? _empresas.where((e) => !e.aprovada).toList() : _empresas;
    return Scaffold(
      appBar: AppBar(title: const Text('Aprovacao de empresas'), actions: const [BotaoSairAdmin()]),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: Row(
              children: [
                const Text('Somente pendentes'),
                Switch(value: _somentePendentes, onChanged: (v) => setState(() => _somentePendentes = v)),
              ],
            ),
          ),
          Expanded(
            child: _carregando
                ? const Center(child: CircularProgressIndicator())
                : _erro != null
                    ? EstadoErro(mensagem: _erro!, aoTentarNovamente: _carregar)
                    : listaFiltrada.isEmpty
                        ? const EstadoVazio(mensagem: 'Nenhuma empresa encontrada.', icone: Icons.storefront_outlined)
                        : RefreshIndicator(
                            onRefresh: _carregar,
                            child: ListView.builder(
                              padding: const EdgeInsets.all(16),
                              itemCount: listaFiltrada.length,
                              itemBuilder: (context, i) => _cartao(listaFiltrada[i]),
                            ),
                          ),
          ),
        ],
      ),
    );
  }

  Widget _cartao(Empresa empresa) {
    final processando = _processando.contains(empresa.id);
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14), side: BorderSide(color: Colors.grey.shade200)),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(child: Text(empresa.nome, style: const TextStyle(fontWeight: FontWeight.bold))),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                  decoration: BoxDecoration(
                    color: (empresa.aprovada ? Colors.green : Colors.orange).withValues(alpha: 0.12),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    empresa.aprovada ? 'Aprovada' : 'Pendente',
                    style: TextStyle(
                      color: empresa.aprovada ? Colors.green : Colors.orange,
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Text('${empresa.categoria.nome} - ${empresa.cidade}/${empresa.uf}', style: const TextStyle(color: Colors.black54, fontSize: 13)),
            if (empresa.nomeResponsavel != null) Text('Responsavel: ${empresa.nomeResponsavel}', style: const TextStyle(fontSize: 12, color: Colors.black45)),
            const SizedBox(height: 10),
            if (processando)
              const Center(child: CircularProgressIndicator(strokeWidth: 2))
            else
              Row(
                children: [
                  if (!empresa.aprovada)
                    Expanded(
                      child: FilledButton(
                        onPressed: () => _definirAprovacao(empresa, true),
                        child: const Text('Aprovar'),
                      ),
                    ),
                  if (!empresa.aprovada) const SizedBox(width: 8),
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () => _definirAprovacao(empresa, false),
                      style: OutlinedButton.styleFrom(foregroundColor: Theme.of(context).colorScheme.error),
                      child: Text(empresa.aprovada ? 'Revogar aprovacao' : 'Reprovar'),
                    ),
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }
}
