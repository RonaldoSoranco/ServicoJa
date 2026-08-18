import '../../../core/network/api_client.dart';
import '../models/categoria.dart';

class CategoriaRepository {
  CategoriaRepository({ApiClient? apiClient}) : _apiClient = apiClient ?? ApiClient();

  final ApiClient _apiClient;

  Future<List<Categoria>> listarAtivas() async {
    final json = await _apiClient.get('/api/categorias') as List<dynamic>;
    return json.map((e) => Categoria.fromJson(e as Map<String, dynamic>)).toList();
  }
}
