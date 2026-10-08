class CategoriaResumo {
  CategoriaResumo({required this.id, required this.nome, this.icone});

  factory CategoriaResumo.fromJson(Map<String, dynamic> json) {
    return CategoriaResumo(
      id: (json['id'] as num).toInt(),
      nome: json['nome'] as String,
      icone: json['icone'] as String?,
    );
  }

  final int id;
  final String nome;
  final String? icone;
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
    this.latitude,
    this.longitude,
    this.distanciaKm,
    required this.abertoAgora,
    required this.temHorarios,
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
      latitude: (json['latitude'] as num?)?.toDouble(),
      longitude: (json['longitude'] as num?)?.toDouble(),
      distanciaKm: (json['distanciaKm'] as num?)?.toDouble(),
      abertoAgora: json['abertoAgora'] as bool? ?? false,
      temHorarios: json['temHorarios'] as bool? ?? false,
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
  final double? latitude;
  final double? longitude;
  final double? distanciaKm;
  final bool abertoAgora;
  final bool temHorarios;
  final bool premiumAtivo;
  final bool destaque;
  final bool perfilCompleto;
  final double? mediaAvaliacoes;
  final int? totalAvaliacoes;

  bool get temLocalizacao => latitude != null && longitude != null;
}
