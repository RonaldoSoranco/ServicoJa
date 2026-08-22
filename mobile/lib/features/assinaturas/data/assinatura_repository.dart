import '../../../core/network/api_client.dart';
import '../models/assinatura.dart';

class AssinaturaRepository {
  AssinaturaRepository({ApiClient? apiClient}) : _apiClient = apiClient ?? ApiClient();

  final ApiClient _apiClient;

  Future<Assinatura> criar({required int empresaId, required TipoAssinatura tipo}) async {
    final json = await _apiClient.post('/api/assinaturas', corpo: {
      'empresaId': empresaId,
      'tipo': tipoAssinaturaParaTexto(tipo),
    }, autenticado: true) as Map<String, dynamic>;
    return Assinatura.fromJson(json);
  }

  Future<Assinatura?> obterAtiva(int empresaId) async {
    final json = await _apiClient.get('/api/assinaturas/empresas/$empresaId', autenticado: true);
    if (json == null) return null;
    return Assinatura.fromJson(json as Map<String, dynamic>);
  }

  Future<String> cancelar(int id) async {
    final json = await _apiClient.delete('/api/assinaturas/$id', autenticado: true) as Map<String, dynamic>;
    return json['mensagem'] as String? ?? 'Assinatura cancelada.';
  }
}
