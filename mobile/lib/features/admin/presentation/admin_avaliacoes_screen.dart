import 'package:flutter/material.dart';

import '../../../core/state/lista_paginada_controller.dart';
import '../../../core/widgets/estrelas.dart';
import '../../../core/widgets/lista_paginada_view.dart';
import '../../avaliacoes/models/avaliacao.dart';
import '../data/admin_repository.dart';
import 'botao_sair_admin.dart';

class AdminAvaliacoesScreen extends StatefulWidget {
  const AdminAvaliacoesScreen({super.key});

  @override
  State<AdminAvaliacoesScreen> createState() => _AdminAvaliacoesScreenState();
}

class _AdminAvaliacoesScreenState extends State<AdminAvaliacoesScreen> {
  final _repositorio = AdminRepository();
  late final ListaPaginadaController<Avaliacao> _controller;
  final Set<int> _processando = {};

  @override
  void initState() {
    super.initState();
    _controller = ListaPaginadaController<Avaliacao>(
      buscar: (pagina, tamanho) => _repositorio.avaliacoesPendentes(pagina: pagina, tamanho: tamanho),
    )..carregarInicial();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _moderar(Avaliacao avaliacao, bool aprovar) async {
    setState(() => _processando.add(avaliacao.id));
    try {
      await _repositorio.moderarAvaliacao(avaliacao.id, aprovar: aprovar);
      _controller.removerLocal((a) => a.id == avaliacao.id);
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Nao foi possivel moderar a avaliacao.')));
      }
    } finally {
      if (mounted) setState(() => _processando.remove(avaliacao.id));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Moderacao de avaliacoes'), actions: const [BotaoSairAdmin()]),
      body: ListaPaginadaView<Avaliacao>(
        controller: _controller,
        mensagemVazia: 'Nenhuma avaliacao pendente de moderacao.',
        iconeVazio: Icons.rate_review_outlined,
        construirItem: (context, avaliacao, indice) => _cartao(avaliacao),
      ),
    );
  }

  Widget _cartao(Avaliacao avaliacao) {
    final processando = _processando.contains(avaliacao.id);
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
                Expanded(child: Text(avaliacao.nomeUsuario, style: const TextStyle(fontWeight: FontWeight.bold))),
                Estrelas(nota: avaliacao.nota.toDouble(), tamanho: 16),
              ],
            ),
            if (avaliacao.comentario != null && avaliacao.comentario!.isNotEmpty) ...[
              const SizedBox(height: 8),
              Text(avaliacao.comentario!),
            ],
            const SizedBox(height: 12),
            if (processando)
              const Center(child: CircularProgressIndicator(strokeWidth: 2))
            else
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () => _moderar(avaliacao, false),
                      style: OutlinedButton.styleFrom(foregroundColor: Theme.of(context).colorScheme.error),
                      child: const Text('Rejeitar'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: FilledButton(
                      onPressed: () => _moderar(avaliacao, true),
                      child: const Text('Aprovar'),
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
