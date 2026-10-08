/// Total no período atual e no anterior (mesmo tamanho), para mostrar a tendência.
class Metrica {
  const Metrica({required this.atual, required this.anterior});

  factory Metrica.fromJson(Map<String, dynamic> json) {
    return Metrica(atual: (json['atual'] as num).toInt(), anterior: (json['anterior'] as num).toInt());
  }

  final int atual;
  final int anterior;

  /// Variação percentual em relação ao período anterior; `null` quando não há base de comparação.
  int? get variacaoPercentual {
    if (anterior == 0) return null;
    return (((atual - anterior) / anterior) * 100).round();
  }
}

/// Painel de desempenho da empresa (`GET /api/empresas/{id}/desempenho`).
class Desempenho {
  const Desempenho({
    required this.dias,
    required this.visualizacoes,
    required this.cliquesWhatsapp,
    required this.cliquesLigar,
    required this.cliquesMapa,
  });

  factory Desempenho.fromJson(Map<String, dynamic> json) {
    return Desempenho(
      dias: (json['dias'] as num).toInt(),
      visualizacoes: Metrica.fromJson(json['visualizacoes'] as Map<String, dynamic>),
      cliquesWhatsapp: Metrica.fromJson(json['cliquesWhatsapp'] as Map<String, dynamic>),
      cliquesLigar: Metrica.fromJson(json['cliquesLigar'] as Map<String, dynamic>),
      cliquesMapa: Metrica.fromJson(json['cliquesMapa'] as Map<String, dynamic>),
    );
  }

  final int dias;
  final Metrica visualizacoes;
  final Metrica cliquesWhatsapp;
  final Metrica cliquesLigar;
  final Metrica cliquesMapa;

  int get totalContatos => cliquesWhatsapp.atual + cliquesLigar.atual + cliquesMapa.atual;
}

/// Interações registradas no perfil da empresa (`POST /api/empresas/{id}/eventos`).
enum TipoEvento {
  visualizacao('VISUALIZACAO'),
  cliqueWhatsapp('CLIQUE_WHATSAPP'),
  cliqueLigar('CLIQUE_LIGAR'),
  cliqueMapa('CLIQUE_MAPA');

  const TipoEvento(this.valor);

  final String valor;
}
