enum TipoAssinatura { mensal, anual }

TipoAssinatura tipoAssinaturaDeTexto(String valor) => valor == 'ANUAL' ? TipoAssinatura.anual : TipoAssinatura.mensal;

String tipoAssinaturaParaTexto(TipoAssinatura tipo) => tipo == TipoAssinatura.anual ? 'ANUAL' : 'MENSAL';

enum StatusAssinatura { aguardandoPagamento, ativa, atrasada, cancelada, expirada }

StatusAssinatura statusAssinaturaDeTexto(String valor) {
  switch (valor) {
    case 'AGUARDANDO_PAGAMENTO':
      return StatusAssinatura.aguardandoPagamento;
    case 'ATIVA':
      return StatusAssinatura.ativa;
    case 'ATRASADA':
      return StatusAssinatura.atrasada;
    case 'CANCELADA':
      return StatusAssinatura.cancelada;
    case 'EXPIRADA':
      return StatusAssinatura.expirada;
    default:
      throw ArgumentError('Status de assinatura desconhecido: $valor');
  }
}

class Assinatura {
  Assinatura({
    required this.id,
    required this.empresaId,
    required this.tipo,
    required this.status,
    this.inicioEm,
    this.fimEm,
    this.valor,
    this.linkPagamento,
    required this.premiumAtivo,
  });

  factory Assinatura.fromJson(Map<String, dynamic> json) {
    return Assinatura(
      id: (json['id'] as num).toInt(),
      empresaId: (json['empresaId'] as num).toInt(),
      tipo: tipoAssinaturaDeTexto(json['tipo'] as String),
      status: statusAssinaturaDeTexto(json['status'] as String),
      inicioEm: json['inicioEm'] != null ? DateTime.tryParse(json['inicioEm'] as String) : null,
      fimEm: json['fimEm'] != null ? DateTime.tryParse(json['fimEm'] as String) : null,
      valor: (json['valor'] as num?)?.toDouble(),
      linkPagamento: json['linkPagamento'] as String?,
      premiumAtivo: json['premiumAtivo'] as bool? ?? false,
    );
  }

  final int id;
  final int empresaId;
  final TipoAssinatura tipo;
  final StatusAssinatura status;
  final DateTime? inicioEm;
  final DateTime? fimEm;
  final double? valor;
  final String? linkPagamento;
  final bool premiumAtivo;
}
