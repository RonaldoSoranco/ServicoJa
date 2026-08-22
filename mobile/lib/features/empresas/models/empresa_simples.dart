class CategoriaResumo {
  CategoriaResumo({required this.id, required this.nome});

  factory CategoriaResumo.fromJson(Map<String, dynamic> json) {
    return CategoriaResumo(id: (json['id'] as num).toInt(), nome: json['nome'] as String);
  }

  final int id;
  final String nome;
}

/// Item de lista da busca publica de empresas (`GET /api/empresas`).
class EmpresaSimples {
  EmpresaSimples({
    required this.id,
    required this.nome,
    required this.categoria,
    this.logoUrl,
    required this.cidade,
    required this.uf,
    required this.premiumAtivo,
    required this.destaque,
    required this.perfilCompleto,
    this.mediaAvaliacoes,
    this.totalAvaliacoes,
  });

  factory EmpresaSimples.fromJson(Map<String, dynamic> json) {
    return EmpresaSimples(
      id: (json['id'] as num).toInt(),
      nome: json['nome'] as String,
      categoria: CategoriaResumo.fromJson(json['categoria'] as Map<String, dynamic>),
      logoUrl: json['logoUrl'] as String?,
      cidade: json['cidade'] as String,
      uf: json['uf'] as String,
      premiumAtivo: json['premiumAtivo'] as bool? ?? false,
      destaque: json['destaque'] as bool? ?? false,
      perfilCompleto: json['perfilCompleto'] as bool? ?? false,
      mediaAvaliacoes: (json['mediaAvaliacoes'] as num?)?.toDouble(),
      totalAvaliacoes: (json['totalAvaliacoes'] as num?)?.toInt(),
    );
  }

  final int id;
  final String nome;
  final CategoriaResumo categoria;
  final String? logoUrl;
  final String cidade;
  final String uf;
  final bool premiumAtivo;
  final bool destaque;
  final bool perfilCompleto;
  final double? mediaAvaliacoes;
  final int? totalAvaliacoes;
}
