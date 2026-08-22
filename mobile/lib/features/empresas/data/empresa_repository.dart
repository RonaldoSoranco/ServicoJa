import '../../../core/network/api_client.dart';
import '../../../core/network/pagina_resposta.dart';
import '../models/empresa.dart';
import '../models/empresa_simples.dart';

class EmpresaRepository {
  EmpresaRepository({ApiClient? apiClient}) : _apiClient = apiClient ?? ApiClient();

  final ApiClient _apiClient;

  Future<PaginaResposta<EmpresaSimples>> buscar({
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
    final json = await _apiClient.get('/api/empresas', query: query) as Map<String, dynamic>;
    return PaginaResposta.fromJson(json, EmpresaSimples.fromJson);
  }

  Future<Empresa> detalhar(int id) async {
    final json = await _apiClient.get('/api/empresas/$id') as Map<String, dynamic>;
    return Empresa.fromJson(json);
  }

  Future<List<Empresa>> listarMinhas() async {
    final json = await _apiClient.get('/api/empresas/minhas', autenticado: true) as List<dynamic>;
    return json.map((e) => Empresa.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<Empresa> criar(EmpresaRequestPayload payload) async {
    final json =
        await _apiClient.post('/api/empresas', corpo: payload.toJson(), autenticado: true) as Map<String, dynamic>;
    return Empresa.fromJson(json);
  }

  Future<Empresa> atualizar(int id, EmpresaRequestPayload payload) async {
    final json = await _apiClient.put('/api/empresas/$id', corpo: payload.toJson(), autenticado: true)
        as Map<String, dynamic>;
    return Empresa.fromJson(json);
  }

  Future<void> excluir(int id) => _apiClient.delete('/api/empresas/$id', autenticado: true);

  Future<Foto> adicionarFoto(int empresaId, {required String url, String? descricao, int? ordem}) async {
    final json = await _apiClient.post('/api/empresas/$empresaId/fotos', corpo: {
      'url': url,
      if (descricao != null && descricao.isNotEmpty) 'descricao': descricao,
      if (ordem != null) 'ordem': ordem,
    }, autenticado: true) as Map<String, dynamic>;
    return Foto.fromJson(json);
  }

  Future<void> removerFoto(int empresaId, int fotoId) =>
      _apiClient.delete('/api/empresas/$empresaId/fotos/$fotoId', autenticado: true);

  Future<Portfolio> adicionarPortfolio(
    int empresaId, {
    String? titulo,
    String? descricao,
    String? urlMidia,
  }) async {
    final json = await _apiClient.post('/api/empresas/$empresaId/portfolios', corpo: {
      if (titulo != null && titulo.isNotEmpty) 'titulo': titulo,
      if (descricao != null && descricao.isNotEmpty) 'descricao': descricao,
      if (urlMidia != null && urlMidia.isNotEmpty) 'urlMidia': urlMidia,
    }, autenticado: true) as Map<String, dynamic>;
    return Portfolio.fromJson(json);
  }

  Future<void> removerPortfolio(int empresaId, int portfolioId) =>
      _apiClient.delete('/api/empresas/$empresaId/portfolios/$portfolioId', autenticado: true);

  Future<String> ativarDestaque(int empresaId) async {
    final json = await _apiClient.post('/api/empresas/$empresaId/destaque', autenticado: true) as Map<String, dynamic>;
    return json['mensagem'] as String? ?? 'Empresa destacada.';
  }

  Future<String> removerDestaque(int empresaId) async {
    final json =
        await _apiClient.delete('/api/empresas/$empresaId/destaque', autenticado: true) as Map<String, dynamic>;
    return json['mensagem'] as String? ?? 'Destaque removido.';
  }
}
