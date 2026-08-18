class ApiException implements Exception {
  ApiException({
    required this.codigo,
    required this.status,
    required this.mensagem,
    this.detalhes = const [],
  });

  factory ApiException.fromBody(int codigoHttp, Map<String, dynamic>? corpo) {
    if (corpo == null) {
      return ApiException(codigo: codigoHttp, status: 'ERRO', mensagem: 'Nao foi possivel completar a requisicao.');
    }
    final detalhesJson = corpo['detalhes'];
    return ApiException(
      codigo: (corpo['codigo'] as num?)?.toInt() ?? codigoHttp,
      status: corpo['status'] as String? ?? 'ERRO',
      mensagem: corpo['mensagem'] as String? ?? 'Nao foi possivel completar a requisicao.',
      detalhes: detalhesJson is List ? detalhesJson.map((e) => e.toString()).toList() : const [],
    );
  }

  factory ApiException.semConexao() => ApiException(
        codigo: 0,
        status: 'SEM_CONEXAO',
        mensagem: 'Nao foi possivel conectar ao servidor. Verifique sua internet.',
      );

  final int codigo;
  final String status;
  final String mensagem;
  final List<String> detalhes;

  @override
  String toString() => detalhes.isEmpty ? mensagem : '$mensagem (${detalhes.join(', ')})';
}
