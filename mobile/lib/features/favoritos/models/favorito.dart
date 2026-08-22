class Favorito {
  Favorito({
    required this.empresaId,
    required this.nomeEmpresa,
    this.logoUrl,
    required this.cidade,
    required this.uf,
    required this.premiumAtivo,
    required this.destaque,
    this.mediaAvaliacoes,
    this.totalAvaliacoes,
  });

  factory Favorito.fromJson(Map<String, dynamic> json) {
    return Favorito(
      empresaId: (json['empresaId'] as num).toInt(),
      nomeEmpresa: json['nomeEmpresa'] as String,
      logoUrl: json['logoUrl'] as String?,
      cidade: json['cidade'] as String,
      uf: json['uf'] as String,
      premiumAtivo: json['premiumAtivo'] as bool? ?? false,
      destaque: json['destaque'] as bool? ?? false,
      mediaAvaliacoes: (json['mediaAvaliacoes'] as num?)?.toDouble(),
      totalAvaliacoes: (json['totalAvaliacoes'] as num?)?.toInt(),
    );
  }

  final int empresaId;
  final String nomeEmpresa;
  final String? logoUrl;
  final String cidade;
  final String uf;
  final bool premiumAtivo;
  final bool destaque;
  final double? mediaAvaliacoes;
  final int? totalAvaliacoes;
}
