enum TipoNotificacao { avaliacao, moderacao, assinatura, pagamento, sistema }

TipoNotificacao tipoNotificacaoDeTexto(String valor) {
  switch (valor) {
    case 'AVALIACAO':
      return TipoNotificacao.avaliacao;
    case 'MODERACAO':
      return TipoNotificacao.moderacao;
    case 'ASSINATURA':
      return TipoNotificacao.assinatura;
    case 'PAGAMENTO':
      return TipoNotificacao.pagamento;
    case 'SISTEMA':
    default:
      return TipoNotificacao.sistema;
  }
}

class Notificacao {
  Notificacao({
    required this.id,
    required this.titulo,
    required this.mensagem,
    required this.lida,
    required this.tipo,
    required this.criadoEm,
  });

  factory Notificacao.fromJson(Map<String, dynamic> json) {
    return Notificacao(
      id: (json['id'] as num).toInt(),
      titulo: json['titulo'] as String,
      mensagem: json['mensagem'] as String,
      lida: json['lida'] as bool? ?? false,
      tipo: tipoNotificacaoDeTexto(json['tipo'] as String),
      criadoEm: DateTime.parse(json['criadoEm'] as String),
    );
  }

  final int id;
  final String titulo;
  final String mensagem;
  final bool lida;
  final TipoNotificacao tipo;
  final DateTime criadoEm;

  Notificacao copiarComo({bool? lida}) {
    return Notificacao(
      id: id,
      titulo: titulo,
      mensagem: mensagem,
      lida: lida ?? this.lida,
      tipo: tipo,
      criadoEm: criadoEm,
    );
  }
}
