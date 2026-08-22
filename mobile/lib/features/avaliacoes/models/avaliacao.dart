enum StatusAvaliacao { pendente, aprovada, rejeitada }

StatusAvaliacao statusAvaliacaoDeTexto(String valor) {
  switch (valor) {
    case 'PENDENTE':
      return StatusAvaliacao.pendente;
    case 'APROVADA':
      return StatusAvaliacao.aprovada;
    case 'REJEITADA':
      return StatusAvaliacao.rejeitada;
    default:
      throw ArgumentError('Status de avaliacao desconhecido: $valor');
  }
}

String statusAvaliacaoParaTexto(StatusAvaliacao status) {
  switch (status) {
    case StatusAvaliacao.pendente:
      return 'PENDENTE';
    case StatusAvaliacao.aprovada:
      return 'APROVADA';
    case StatusAvaliacao.rejeitada:
      return 'REJEITADA';
  }
}

class Avaliacao {
  Avaliacao({
    required this.id,
    required this.usuarioId,
    required this.nomeUsuario,
    required this.nota,
    this.comentario,
    required this.status,
    required this.criadoEm,
  });

  factory Avaliacao.fromJson(Map<String, dynamic> json) {
    return Avaliacao(
      id: (json['id'] as num).toInt(),
      usuarioId: (json['usuarioId'] as num).toInt(),
      nomeUsuario: json['nomeUsuario'] as String,
      nota: (json['nota'] as num).toInt(),
      comentario: json['comentario'] as String?,
      status: statusAvaliacaoDeTexto(json['status'] as String),
      criadoEm: DateTime.parse(json['criadoEm'] as String),
    );
  }

  final int id;
  final int usuarioId;
  final String nomeUsuario;
  final int nota;
  final String? comentario;
  final StatusAvaliacao status;
  final DateTime criadoEm;
}
