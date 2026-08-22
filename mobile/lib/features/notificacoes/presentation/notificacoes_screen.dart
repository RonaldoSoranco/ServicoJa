import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';

import '../../../core/state/lista_paginada_controller.dart';
import '../../../core/widgets/lista_paginada_view.dart';
import '../data/notificacao_repository.dart';
import '../models/notificacao.dart';
import '../state/notificacao_badge_controller.dart';

class NotificacoesScreen extends StatefulWidget {
  const NotificacoesScreen({super.key});

  @override
  State<NotificacoesScreen> createState() => _NotificacoesScreenState();
}

class _NotificacoesScreenState extends State<NotificacoesScreen> {
  final _repositorio = NotificacaoRepository();
  final _formatoData = DateFormat('dd/MM/yyyy HH:mm', 'pt_BR');
  late final ListaPaginadaController<Notificacao> _controller;

  @override
  void initState() {
    super.initState();
    _controller = ListaPaginadaController<Notificacao>(
      buscar: (pagina, tamanho) => _repositorio.listar(pagina: pagina, tamanho: tamanho),
    )..carregarInicial();
    // Ao abrir a aba, sincroniza o contador do badge do shell.
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) context.read<NotificacaoBadgeController>().atualizar();
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  IconData _iconePorTipo(TipoNotificacao tipo) {
    switch (tipo) {
      case TipoNotificacao.avaliacao:
        return Icons.star_outline;
      case TipoNotificacao.moderacao:
        return Icons.gavel_outlined;
      case TipoNotificacao.assinatura:
        return Icons.workspace_premium_outlined;
      case TipoNotificacao.pagamento:
        return Icons.payments_outlined;
      case TipoNotificacao.sistema:
        return Icons.info_outline;
    }
  }

  Future<void> _marcarLida(Notificacao notificacao) async {
    if (notificacao.lida) return;
    final indice = _controller.itens.indexWhere((n) => n.id == notificacao.id);
    if (indice == -1) return;
    final novaLista = [..._controller.itens];
    novaLista[indice] = notificacao.copiarComo(lida: true);
    _controller.substituirItens(novaLista);
    try {
      await _repositorio.marcarLida(notificacao.id);
      if (mounted) context.read<NotificacaoBadgeController>().atualizar();
    } catch (_) {
      // Mantem marcada localmente; a proxima atualizacao sincroniza o estado real.
    }
  }

  Future<void> _marcarTodasLidas() async {
    try {
      await _repositorio.marcarTodasLidas();
      final novaLista = _controller.itens.map((n) => n.copiarComo(lida: true)).toList();
      _controller.substituirItens(novaLista);
      if (mounted) context.read<NotificacaoBadgeController>().zerar();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(const SnackBar(content: Text('Nao foi possivel marcar todas como lidas.')));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Notificacoes'),
        actions: [
          TextButton(
            onPressed: _marcarTodasLidas,
            child: const Text('Marcar todas'),
          ),
        ],
      ),
      body: ListaPaginadaView<Notificacao>(
        controller: _controller,
        mensagemVazia: 'Voce nao tem notificacoes.',
        iconeVazio: Icons.notifications_none,
        construirItem: (context, notificacao, indice) => Card(
          margin: const EdgeInsets.only(bottom: 10),
          elevation: 0,
          color: notificacao.lida ? Colors.white : Theme.of(context).colorScheme.primary.withValues(alpha: 0.05),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
            side: BorderSide(color: Colors.grey.shade200),
          ),
          child: ListTile(
            leading: Icon(_iconePorTipo(notificacao.tipo), color: notificacao.lida ? Colors.black45 : Theme.of(context).colorScheme.primary),
            title: Text(
              notificacao.titulo,
              style: TextStyle(fontWeight: notificacao.lida ? FontWeight.normal : FontWeight.bold),
            ),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(notificacao.mensagem),
                const SizedBox(height: 4),
                Text(_formatoData.format(notificacao.criadoEm), style: const TextStyle(fontSize: 11, color: Colors.black45)),
              ],
            ),
            trailing: notificacao.lida ? null : Container(
              width: 10,
              height: 10,
              decoration: BoxDecoration(color: Theme.of(context).colorScheme.primary, shape: BoxShape.circle),
            ),
            onTap: () => _marcarLida(notificacao),
          ),
        ),
      ),
    );
  }
}
