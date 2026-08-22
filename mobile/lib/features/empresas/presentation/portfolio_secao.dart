import 'package:flutter/material.dart';

import '../models/empresa.dart';
import 'secao_bloqueada_premium.dart';

/// Secao "Portfolio" da tela de edicao de empresa (Fase 8) — exclusiva para Premium.
class PortfolioSecao extends StatelessWidget {
  const PortfolioSecao({
    super.key,
    required this.premiumAtivo,
    required this.portfolios,
    required this.aoAdicionar,
    required this.aoRemover,
  });

  final bool premiumAtivo;
  final List<Portfolio> portfolios;
  final Future<void> Function(String? titulo, String? descricao, String? urlMidia) aoAdicionar;
  final Future<void> Function(Portfolio portfolio) aoRemover;

  Future<void> _abrirDialogoAdicionar(BuildContext context) async {
    final tituloController = TextEditingController();
    final descricaoController = TextEditingController();
    final urlController = TextEditingController();
    final resultado = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Adicionar item ao portfolio'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(controller: tituloController, decoration: const InputDecoration(labelText: 'Titulo (opcional)')),
              const SizedBox(height: 8),
              TextField(
                controller: descricaoController,
                maxLines: 2,
                decoration: const InputDecoration(labelText: 'Descricao (opcional)'),
              ),
              const SizedBox(height: 8),
              TextField(
                controller: urlController,
                decoration: const InputDecoration(labelText: 'URL da midia (opcional)'),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Cancelar')),
          FilledButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('Adicionar')),
        ],
      ),
    );
    if (resultado != true) return;
    final titulo = tituloController.text.trim();
    final descricao = descricaoController.text.trim();
    final url = urlController.text.trim();
    if (titulo.isEmpty && descricao.isEmpty && url.isEmpty) {
      if (context.mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(const SnackBar(content: Text('Preencha ao menos um dos campos.')));
      }
      return;
    }
    await aoAdicionar(titulo.isEmpty ? null : titulo, descricao.isEmpty ? null : descricao, url.isEmpty ? null : url);
  }

  @override
  Widget build(BuildContext context) {
    if (!premiumAtivo) {
      return const SecaoBloqueadaPremium(
        titulo: 'Portfolio (Premium)',
        mensagem: 'O portfolio e exclusivo para empresas Premium. Assine para liberar esta secao.',
      );
    }
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            const Expanded(child: Text('Portfolio', style: TextStyle(fontWeight: FontWeight.bold))),
            TextButton.icon(
              onPressed: () => _abrirDialogoAdicionar(context),
              icon: const Icon(Icons.add),
              label: const Text('Adicionar'),
            ),
          ],
        ),
        if (portfolios.isEmpty)
          const Padding(
            padding: EdgeInsets.symmetric(vertical: 8),
            child: Text('Nenhum item no portfolio ainda.', style: TextStyle(color: Colors.black45)),
          )
        else
          ...portfolios.map((p) => _item(context, p)),
      ],
    );
  }

  Widget _item(BuildContext context, Portfolio portfolio) {
    return Container(
      margin: const EdgeInsets.only(top: 8),
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(border: Border.all(color: Colors.grey.shade200), borderRadius: BorderRadius.circular(10)),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (portfolio.titulo != null && portfolio.titulo!.isNotEmpty)
                  Text(portfolio.titulo!, style: const TextStyle(fontWeight: FontWeight.w600)),
                if (portfolio.descricao != null && portfolio.descricao!.isNotEmpty) Text(portfolio.descricao!),
                if (portfolio.urlMidia != null && portfolio.urlMidia!.isNotEmpty)
                  Text(portfolio.urlMidia!, style: const TextStyle(fontSize: 12, color: Colors.black45)),
              ],
            ),
          ),
          IconButton(
            icon: const Icon(Icons.delete_outline, size: 20),
            onPressed: () => aoRemover(portfolio),
          ),
        ],
      ),
    );
  }
}
