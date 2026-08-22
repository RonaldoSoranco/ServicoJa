import '../../../core/network/api_client.dart';
import '../../../core/network/pagina_resposta.dart';
import '../models/avaliacao.dart';

class AvaliacaoRepository {
  AvaliacaoRepository({ApiClient? apiClient}) : _apiClient = apiClient ?? ApiClient();

  final ApiClient _apiClient;

  Future<Avaliacao> avaliar(int empresaId, {required int nota, String? comentario}) async {
    final json = await _apiClient.post('/api/avaliacoes/empresas/$empresaId', corpo: {
      'nota': nota,
      if (comentario != null && comentario.isNotEmpty) 'comentario': comentario,
    }, autenticado: true) as Map<String, dynamic>;
    return Avaliacao.fromJson(json);
  }

  Future<PaginaResposta<Avaliacao>> listarPorEmpresa(int empresaId, {required int pagina, required int tamanho}) async {
    final json = await _apiClient.get('/api/avaliacoes/empresas/$empresaId', query: {
      'pagina': '$pagina',
      'tamanho': '$tamanho',
    }) as Map<String, dynamic>;
    return PaginaResposta.fromJson(json, Avaliacao.fromJson);
  }

  Future<List<Avaliacao>> listarMinhas() async {
    final json = await _apiClient.get('/api/avaliacoes/minhas', autenticado: true) as List<dynamic>;
    return json.map((e) => Avaliacao.fromJson(e as Map<String, dynamic>)).toList();
  }
}
