import '../../../core/network/api_client.dart';
import '../../../core/network/pagina_resposta.dart';
import '../models/notificacao.dart';

class NotificacaoRepository {
  NotificacaoRepository({ApiClient? apiClient}) : _apiClient = apiClient ?? ApiClient();

  final ApiClient _apiClient;

  Future<PaginaResposta<Notificacao>> listar({required int pagina, required int tamanho}) async {
    final json = await _apiClient.get('/api/notificacoes', autenticado: true, query: {
      'pagina': '$pagina',
      'tamanho': '$tamanho',
    }) as Map<String, dynamic>;
    return PaginaResposta.fromJson(json, Notificacao.fromJson);
  }

  Future<int> contarNaoLidas() async {
    final json = await _apiClient.get('/api/notificacoes/nao-lidas', autenticado: true) as Map<String, dynamic>;
    return (json['quantidade'] as num?)?.toInt() ?? 0;
  }

  Future<Notificacao> marcarLida(int id) async {
    final json = await _apiClient.patch('/api/notificacoes/$id/lida', autenticado: true) as Map<String, dynamic>;
    return Notificacao.fromJson(json);
  }

  Future<void> marcarTodasLidas() => _apiClient.patch('/api/notificacoes/lidas', autenticado: true);
}
