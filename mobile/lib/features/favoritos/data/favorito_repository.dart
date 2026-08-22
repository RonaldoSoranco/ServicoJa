import '../../../core/network/api_client.dart';
import '../../../core/network/pagina_resposta.dart';
import '../models/favorito.dart';

class FavoritoRepository {
  FavoritoRepository({ApiClient? apiClient}) : _apiClient = apiClient ?? ApiClient();

  final ApiClient _apiClient;

  Future<String> favoritar(int empresaId) async {
    final json =
        await _apiClient.post('/api/favoritos/empresas/$empresaId', autenticado: true) as Map<String, dynamic>;
    return json['mensagem'] as String? ?? 'Empresa favoritada.';
  }

  Future<String> remover(int empresaId) async {
    final json =
        await _apiClient.delete('/api/favoritos/empresas/$empresaId', autenticado: true) as Map<String, dynamic>;
    return json['mensagem'] as String? ?? 'Empresa removida dos favoritos.';
  }

  Future<PaginaResposta<Favorito>> listar({required int pagina, required int tamanho}) async {
    final json = await _apiClient.get('/api/favoritos', autenticado: true, query: {
      'pagina': '$pagina',
      'tamanho': '$tamanho',
    }) as Map<String, dynamic>;
    return PaginaResposta.fromJson(json, Favorito.fromJson);
  }

  Future<bool> estaFavoritada(int empresaId) async {
    final json = await _apiClient.get(
      '/api/favoritos/estado',
      autenticado: true,
      query: {'empresaId': '$empresaId'},
    ) as Map<String, dynamic>;
    return json['favoritado'] as bool? ?? false;
  }
}
