import 'package:flutter/material.dart';

import '../state/lista_paginada_controller.dart';
import 'estado_erro.dart';
import 'estado_vazio.dart';

/// ListView + RefreshIndicator + scroll infinito ligado a um [ListaPaginadaController].
class ListaPaginadaView<T> extends StatefulWidget {
  const ListaPaginadaView({
    super.key,
    required this.controller,
    required this.construirItem,
    this.mensagemVazia = 'Nada encontrado.',
    this.iconeVazio,
    this.acaoVazia,
    this.rotuloAcaoVazia,
    this.padding,
    this.cabecalho,
  });

  final ListaPaginadaController<T> controller;
  final Widget Function(BuildContext context, T item, int indice) construirItem;
  final String mensagemVazia;
  final IconData? iconeVazio;
  final VoidCallback? acaoVazia;
  final String? rotuloAcaoVazia;
  final EdgeInsetsGeometry? padding;

  /// Widget fixo exibido acima da lista (ex.: filtros), some no estado vazio/erro tambem
  /// se o chamador optar por incluir a logica de filtro dentro do proprio cabecalho.
  final Widget? cabecalho;

  @override
  State<ListaPaginadaView<T>> createState() => _ListaPaginadaViewState<T>();
}

class _ListaPaginadaViewState<T> extends State<ListaPaginadaView<T>> {
  final _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_aoRolar);
  }

  void _aoRolar() {
    if (!_scrollController.hasClients) return;
    if (_scrollController.position.pixels >= _scrollController.position.maxScrollExtent - 200) {
      widget.controller.carregarMais();
    }
  }

  @override
  void dispose() {
    _scrollController.removeListener(_aoRolar);
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: widget.controller,
      builder: (context, _) {
        final controller = widget.controller;
        Widget corpo;
        if (controller.carregando && controller.itens.isEmpty) {
          corpo = const Center(child: CircularProgressIndicator());
        } else if (controller.erro != null && controller.itens.isEmpty) {
          corpo = EstadoErro(mensagem: controller.erro!, aoTentarNovamente: controller.carregarInicial);
        } else if (controller.itens.isEmpty) {
          corpo = EstadoVazio(
            mensagem: widget.mensagemVazia,
            icone: widget.iconeVazio ?? Icons.inbox_outlined,
            acao: widget.acaoVazia,
            rotuloAcao: widget.rotuloAcaoVazia,
          );
        } else {
          corpo = RefreshIndicator(
            onRefresh: controller.atualizar,
            child: ListView.builder(
              controller: _scrollController,
              padding: widget.padding ?? const EdgeInsets.all(16),
              itemCount: controller.itens.length + (controller.temMais ? 1 : 0),
              itemBuilder: (context, index) {
                if (index >= controller.itens.length) {
                  return const Padding(
                    padding: EdgeInsets.symmetric(vertical: 16),
                    child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
                  );
                }
                return widget.construirItem(context, controller.itens[index], index);
              },
            ),
          );
        }
        if (widget.cabecalho == null) return corpo;
        return Column(
          children: [
            widget.cabecalho!,
            Expanded(child: corpo),
          ],
        );
      },
    );
  }
}
