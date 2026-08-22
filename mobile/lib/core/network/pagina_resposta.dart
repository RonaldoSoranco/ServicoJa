/// Parser generico do envelope de paginacao padrao do backend.
///
/// Formato: `{conteudo, pagina, tamanho, totalElementos, totalPaginas}`.
class PaginaResposta<T> {
  PaginaResposta({
    required this.conteudo,
    required this.pagina,
    required this.tamanho,
    required this.totalElementos,
    required this.totalPaginas,
  });

  factory PaginaResposta.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) itemFromJson,
  ) {
    final lista = (json['conteudo'] as List<dynamic>? ?? const [])
        .map((e) => itemFromJson(e as Map<String, dynamic>))
        .toList();
    return PaginaResposta<T>(
      conteudo: lista,
      pagina: (json['pagina'] as num?)?.toInt() ?? 0,
      tamanho: (json['tamanho'] as num?)?.toInt() ?? lista.length,
      totalElementos: (json['totalElementos'] as num?)?.toInt() ?? lista.length,
      totalPaginas: (json['totalPaginas'] as num?)?.toInt() ?? 1,
    );
  }

  final List<T> conteudo;
  final int pagina;
  final int tamanho;
  final int totalElementos;
  final int totalPaginas;

  bool get temProximaPagina => pagina + 1 < totalPaginas;
}
