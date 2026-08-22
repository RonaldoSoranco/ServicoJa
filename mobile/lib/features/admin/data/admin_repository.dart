import '../../../core/network/api_client.dart';
import '../../../core/network/pagina_resposta.dart';
import '../../avaliacoes/models/avaliacao.dart';
import '../../empresas/models/empresa.dart';
import '../models/estatisticas.dart';
import '../models/log_admin.dart';
import '../models/usuario_admin.dart';

class AdminRepository {
  AdminRepository({ApiClient? apiClient}) : _apiClient = apiClient ?? ApiClient();

  final ApiClient _apiClient;

  Future<Estatisticas> estatisticas() async {
    final json = await _apiClient.get('/api/admin/estatisticas', autenticado: true) as Map<String, dynamic>;
    return Estatisticas.fromJson(json);
  }

  /// A API nao filtra por aprovacao no servidor: buscamos com `tamanho` maior
  /// e filtramos `aprovada == false` no cliente quando `somentePendentes` for true.
  Future<PaginaResposta<Empresa>> listarEmpresas({
    int? categoriaId,
    String? nome,
    String? cidade,
    String? uf,
    required int pagina,
    required int tamanho,
  }) async {
    final query = <String, String>{
      'pagina': '$pagina',
      'tamanho': '$tamanho',
      if (categoriaId != null) 'categoriaId': '$categoriaId',
      if (nome != null && nome.trim().isNotEmpty) 'nome': nome.trim(),
      if (cidade != null && cidade.trim().isNotEmpty) 'cidade': cidade.trim(),
      if (uf != null && uf.trim().isNotEmpty) 'uf': uf.trim(),
    };
    final json = await _apiClient.get('/api/admin/empresas', autenticado: true, query: query) as Map<String, dynamic>;
    return PaginaResposta.fromJson(json, Empresa.fromJson);
  }

  Future<Empresa> definirAprovacao(int empresaId, bool aprovada) async {
    // O endpoint recebe `aprovada` como query param (sem corpo).
    final json = await _apiClient.patch(
      '/api/admin/empresas/$empresaId/aprovacao?aprovada=$aprovada',
      autenticado: true,
    ) as Map<String, dynamic>;
    return Empresa.fromJson(json);
  }

  Future<PaginaResposta<Avaliacao>> avaliacoesPendentes({required int pagina, required int tamanho}) async {
    final json = await _apiClient.get('/api/admin/avaliacoes/pendentes', autenticado: true, query: {
      'pagina': '$pagina',
      'tamanho': '$tamanho',
    }) as Map<String, dynamic>;
    return PaginaResposta.fromJson(json, Avaliacao.fromJson);
  }

  Future<Avaliacao> moderarAvaliacao(int id, {required bool aprovar}) async {
    final json = await _apiClient.patch('/api/admin/avaliacoes/$id/moderacao', autenticado: true, corpo: {
      'status': aprovar ? 'APROVADA' : 'REJEITADA',
    }) as Map<String, dynamic>;
    return Avaliacao.fromJson(json);
  }

  Future<PaginaResposta<UsuarioAdmin>> listarUsuarios({required int pagina, required int tamanho}) async {
    final json = await _apiClient.get('/api/admin/usuarios', autenticado: true, query: {
      'pagina': '$pagina',
      'tamanho': '$tamanho',
    }) as Map<String, dynamic>;
    return PaginaResposta.fromJson(json, UsuarioAdmin.fromJson);
  }

  Future<PaginaResposta<LogAdmin>> listarLogs({required int pagina, int tamanho = 20}) async {
    final json = await _apiClient.get('/api/admin/logs', autenticado: true, query: {
      'pagina': '$pagina',
      'tamanho': '$tamanho',
    }) as Map<String, dynamic>;
    return PaginaResposta.fromJson(json, LogAdmin.fromJson);
  }
}
