import 'package:flutter/foundation.dart';

import '../data/notificacao_repository.dart';

/// Mantem a contagem de notificacoes nao lidas para exibir o badge no shell.
///
/// Atualizado ao entrar na aba de notificacoes e apos marcar itens como lidos
/// (nao ha push/WebSocket no MVP, entao a contagem so muda com acao do usuario).
class NotificacaoBadgeController extends ChangeNotifier {
  NotificacaoBadgeController({NotificacaoRepository? repositorio}) : _repositorio = repositorio ?? NotificacaoRepository();

  final NotificacaoRepository _repositorio;

  int quantidade = 0;

  Future<void> atualizar() async {
    try {
      quantidade = await _repositorio.contarNaoLidas();
      notifyListeners();
    } catch (_) {
      // Mantem o ultimo valor conhecido em caso de falha pontual.
    }
  }

  void zerar() {
    quantidade = 0;
    notifyListeners();
  }
}
