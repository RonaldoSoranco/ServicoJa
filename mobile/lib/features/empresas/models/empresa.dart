import 'empresa_simples.dart';

class Foto {
  Foto({required this.id, required this.url, this.descricao, this.ordem});

  factory Foto.fromJson(Map<String, dynamic> json) {
    return Foto(
      id: (json['id'] as num).toInt(),
      url: json['url'] as String,
      descricao: json['descricao'] as String?,
      ordem: (json['ordem'] as num?)?.toInt(),
    );
  }

  final int id;
  final String url;
  final String? descricao;
  final int? ordem;
}

class Portfolio {
  Portfolio({required this.id, this.titulo, this.descricao, this.urlMidia});

  factory Portfolio.fromJson(Map<String, dynamic> json) {
    return Portfolio(
      id: (json['id'] as num).toInt(),
      titulo: json['titulo'] as String?,
      descricao: json['descricao'] as String?,
      urlMidia: json['urlMidia'] as String?,
    );
  }

  final int id;
  final String? titulo;
  final String? descricao;
  final String? urlMidia;
}

/// Perfil completo de uma empresa (`GET /api/empresas/{id}` e `/minhas`).
class Empresa {
  Empresa({
    required this.id,
    required this.nome,
    required this.categoria,
    this.nomeResponsavel,
    this.descricaoCurta,
    this.descricaoCompleta,
    this.logoUrl,
    this.telefone,
    this.whatsapp,
    this.emailContato,
    this.cep,
    this.endereco,
    this.numero,
    this.bairro,
    required this.cidade,
    required this.uf,
    this.latitude,
    this.longitude,
    this.horarioFuncionamento,
    this.redesSociais,
    this.site,
    required this.premiumAtivo,
    this.premiumAte,
    required this.destaque,
    required this.aprovada,
    required this.perfilCompleto,
    this.mediaAvaliacoes,
    this.totalAvaliacoes,
    required this.fotos,
    required this.portfolios,
  });

  factory Empresa.fromJson(Map<String, dynamic> json) {
    return Empresa(
      id: (json['id'] as num).toInt(),
      nome: json['nome'] as String,
      categoria: CategoriaResumo.fromJson(json['categoria'] as Map<String, dynamic>),
      nomeResponsavel: json['nomeResponsavel'] as String?,
      descricaoCurta: json['descricaoCurta'] as String?,
      descricaoCompleta: json['descricaoCompleta'] as String?,
      logoUrl: json['logoUrl'] as String?,
      telefone: json['telefone'] as String?,
      whatsapp: json['whatsapp'] as String?,
      emailContato: json['emailContato'] as String?,
      cep: json['cep'] as String?,
      endereco: json['endereco'] as String?,
      numero: json['numero'] as String?,
      bairro: json['bairro'] as String?,
      cidade: json['cidade'] as String,
      uf: json['uf'] as String,
      latitude: (json['latitude'] as num?)?.toDouble(),
      longitude: (json['longitude'] as num?)?.toDouble(),
      horarioFuncionamento: json['horarioFuncionamento'] as String?,
      redesSociais: json['redesSociais'] as String?,
      site: json['site'] as String?,
      premiumAtivo: json['premiumAtivo'] as bool? ?? false,
      premiumAte: json['premiumAte'] != null ? DateTime.tryParse(json['premiumAte'] as String) : null,
      destaque: json['destaque'] as bool? ?? false,
      aprovada: json['aprovada'] as bool? ?? false,
      perfilCompleto: json['perfilCompleto'] as bool? ?? false,
      mediaAvaliacoes: (json['mediaAvaliacoes'] as num?)?.toDouble(),
      totalAvaliacoes: (json['totalAvaliacoes'] as num?)?.toInt(),
      fotos: (json['fotos'] as List<dynamic>? ?? const [])
          .map((e) => Foto.fromJson(e as Map<String, dynamic>))
          .toList(),
      portfolios: (json['portfolios'] as List<dynamic>? ?? const [])
          .map((e) => Portfolio.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }

  final int id;
  final String nome;
  final CategoriaResumo categoria;
  final String? nomeResponsavel;
  final String? descricaoCurta;
  final String? descricaoCompleta;
  final String? logoUrl;
  final String? telefone;
  final String? whatsapp;
  final String? emailContato;
  final String? cep;
  final String? endereco;
  final String? numero;
  final String? bairro;
  final String cidade;
  final String uf;
  final double? latitude;
  final double? longitude;
  final String? horarioFuncionamento;
  final String? redesSociais;
  final String? site;
  final bool premiumAtivo;
  final DateTime? premiumAte;
  final bool destaque;
  final bool aprovada;
  final bool perfilCompleto;
  final double? mediaAvaliacoes;
  final int? totalAvaliacoes;
  final List<Foto> fotos;
  final List<Portfolio> portfolios;
}

/// Corpo de `POST/PUT /api/empresas` — usado pelo formulario de cadastro/edicao.
class EmpresaRequestPayload {
  EmpresaRequestPayload({
    required this.nome,
    required this.categoriaId,
    this.descricaoCurta,
    this.descricaoCompleta,
    this.logoUrl,
    this.telefone,
    this.whatsapp,
    this.emailContato,
    this.cep,
    this.endereco,
    this.numero,
    this.bairro,
    required this.cidade,
    required this.uf,
    this.latitude,
    this.longitude,
    this.horarioFuncionamento,
    this.redesSociais,
    this.site,
  });

  final String nome;
  final int categoriaId;
  final String? descricaoCurta;
  final String? descricaoCompleta;
  final String? logoUrl;
  final String? telefone;
  final String? whatsapp;
  final String? emailContato;
  final String? cep;
  final String? endereco;
  final String? numero;
  final String? bairro;
  final String cidade;
  final String uf;
  final double? latitude;
  final double? longitude;
  final String? horarioFuncionamento;
  final String? redesSociais;
  final String? site;

  Map<String, dynamic> toJson() {
    return {
      'nome': nome,
      'categoriaId': categoriaId,
      'descricaoCurta': descricaoCurta ?? '',
      'descricaoCompleta': descricaoCompleta ?? '',
      'logoUrl': logoUrl ?? '',
      'telefone': telefone ?? '',
      'whatsapp': whatsapp ?? '',
      'emailContato': emailContato ?? '',
      'cep': cep ?? '',
      'endereco': endereco ?? '',
      'numero': numero ?? '',
      'bairro': bairro ?? '',
      'cidade': cidade,
      'uf': uf,
      'latitude': latitude,
      'longitude': longitude,
      'horarioFuncionamento': horarioFuncionamento ?? '',
      'redesSociais': redesSociais ?? '',
      'site': site ?? '',
    };
  }
}
