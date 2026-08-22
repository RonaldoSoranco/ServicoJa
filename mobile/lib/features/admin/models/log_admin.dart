class LogAdmin {
  LogAdmin({
    required this.id,
    required this.usuario,
    required this.acao,
    this.detalhes,
    this.ip,
    required this.criadoEm,
  });

  factory LogAdmin.fromJson(Map<String, dynamic> json) {
    return LogAdmin(
      id: (json['id'] as num).toInt(),
      usuario: json['usuario'] as String? ?? 'Sistema',
      acao: json['acao'] as String,
      detalhes: json['detalhes'] as String?,
      ip: json['ip'] as String?,
      criadoEm: DateTime.parse(json['criadoEm'] as String),
    );
  }

  final int id;
  final String usuario;
  final String acao;
  final String? detalhes;
  final String? ip;
  final DateTime criadoEm;
}
