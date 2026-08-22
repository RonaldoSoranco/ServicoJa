import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/estado_erro.dart';
import '../../../core/widgets/estado_vazio.dart';
import '../data/empresa_repository.dart';
import '../models/empresa.dart';
import 'empresa_formulario_screen.dart';

class MinhasEmpresasScreen extends StatefulWidget {
  const MinhasEmpresasScreen({super.key});

  @override
  State<MinhasEmpresasScreen> createState() => _MinhasEmpresasScreenState();
}

class _MinhasEmpresasScreenState extends State<MinhasEmpresasScreen> {
  final _repositorio = EmpresaRepository();
  bool _carregando = true;
  String? _erro;
  List<Empresa> _empresas = [];

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
      final empresas = await _repositorio.listarMinhas();
      if (mounted) setState(() => _empresas = empresas);
    } on ApiException catch (e) {
      if (mounted) setState(() => _erro = e.toString());
    } catch (_) {
      if (mounted) setState(() => _erro = 'Ocorreu um erro inesperado. Tente novamente.');
    } finally {
      if (mounted) setState(() => _carregando = false);
    }
  }

  Future<void> _abrirFormulario({Empresa? empresa}) async {
    await Navigator.of(context).push(
      MaterialPageRoute(builder: (_) => EmpresaFormularioScreen(empresa: empresa)),
    );
    _carregar();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Minha empresa')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _abrirFormulario(),
        icon: const Icon(Icons.add),
        label: const Text('Nova empresa'),
      ),
      body: _carregando
          ? const Center(child: CircularProgressIndicator())
          : _erro != null
              ? EstadoErro(mensagem: _erro!, aoTentarNovamente: _carregar)
              : _empresas.isEmpty
                  ? EstadoVazio(
                      mensagem: 'Voce ainda nao cadastrou nenhuma empresa.',
                      icone: Icons.storefront_outlined,
                      acao: () => _abrirFormulario(),
                      rotuloAcao: 'Cadastrar empresa',
                    )
                  : RefreshIndicator(
                      onRefresh: _carregar,
                      child: ListView.builder(
                        padding: const EdgeInsets.fromLTRB(16, 16, 16, 88),
                        itemCount: _empresas.length,
                        itemBuilder: (context, i) => _cartao(_empresas[i]),
                      ),
                    ),
    );
  }

  Widget _cartao(Empresa empresa) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14), side: BorderSide(color: Colors.grey.shade200)),
      child: ListTile(
        contentPadding: const EdgeInsets.all(14),
        title: Text(empresa.nome, style: const TextStyle(fontWeight: FontWeight.bold)),
        subtitle: Padding(
          padding: const EdgeInsets.only(top: 6),
          child: Wrap(
            spacing: 6,
            runSpacing: 6,
            children: [
              _badge(empresa.aprovada ? 'Aprovada' : 'Pendente', empresa.aprovada ? Colors.green : Colors.orange),
              if (empresa.premiumAtivo) _badge('Premium', Colors.amber.shade800),
              if (empresa.destaque) _badge('Destaque', Theme.of(context).colorScheme.secondary),
            ],
          ),
        ),
        trailing: const Icon(Icons.chevron_right, color: Colors.black38),
        onTap: () => _abrirFormulario(empresa: empresa),
      ),
    );
  }

  Widget _badge(String texto, Color cor) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(color: cor.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(8)),
      child: Text(texto, style: TextStyle(color: cor, fontSize: 11, fontWeight: FontWeight.bold)),
    );
  }
}
