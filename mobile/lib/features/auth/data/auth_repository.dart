import '../../../core/network/api_client.dart';
import '../../../core/storage/token_storage.dart';
import '../models/sessao.dart';
import '../models/usuario.dart';

class AuthRepository {
  AuthRepository({ApiClient? apiClient, TokenStorage? tokenStorage})
      : _apiClient = apiClient ?? ApiClient(),
        _tokenStorage = tokenStorage ?? TokenStorage();

  final ApiClient _apiClient;
  final TokenStorage _tokenStorage;

  Future<Sessao> login({required String email, required String senha}) async {
    final json = await _apiClient.post('/api/auth/login', corpo: {'email': email, 'senha': senha});
    final sessao = Sessao.fromJson(json as Map<String, dynamic>);
    await _tokenStorage.salvar(tokenAcesso: sessao.tokenAcesso, tokenRefresh: sessao.tokenRefresh);
    return sessao;
  }

  Future<Sessao> cadastrarCliente({
    required String nome,
    required String email,
    required String senha,
    String? telefone,
  }) async {
    final json = await _apiClient.post('/api/auth/cadastro/cliente', corpo: {
      'nome': nome,
      'email': email,
      'senha': senha,
      'telefone': telefone ?? '',
    });
    final sessao = Sessao.fromJson(json as Map<String, dynamic>);
    await _tokenStorage.salvar(tokenAcesso: sessao.tokenAcesso, tokenRefresh: sessao.tokenRefresh);
    return sessao;
  }

  Future<Sessao> cadastrarEmpresa({
    required String nomeResponsavel,
    required String email,
    required String senha,
    String? telefone,
    required String nomeEmpresa,
    required int categoriaId,
    required String cidade,
    required String uf,
    String? cpf,
  }) async {
    final json = await _apiClient.post('/api/auth/cadastro/empresa', corpo: {
      'nomeResponsavel': nomeResponsavel,
      'email': email,
      'senha': senha,
      'telefone': telefone ?? '',
      'nomeEmpresa': nomeEmpresa,
      'categoriaId': categoriaId,
      'cidade': cidade,
      'uf': uf,
      'cpf': cpf ?? '',
    });
    final sessao = Sessao.fromJson(json as Map<String, dynamic>);
    await _tokenStorage.salvar(tokenAcesso: sessao.tokenAcesso, tokenRefresh: sessao.tokenRefresh);
    return sessao;
  }

  Future<Usuario?> sessaoAtual() async {
    try {
      final token = await _tokenStorage.lerTokenAcesso();
      if (token == null) return null;
      final json = await _apiClient.get('/api/auth/me', autenticado: true);
      return Usuario.fromJson(json as Map<String, dynamic>);
    } catch (_) {
      await _tokenStorage.limpar();
      return null;
    }
  }

  Future<void> logout() async {
    final tokenRefresh = await _tokenStorage.lerTokenRefresh();
    try {
      if (tokenRefresh != null) {
        await _apiClient.post('/api/auth/logout', corpo: {'tokenRefresh': tokenRefresh});
      }
    } catch (_) {
      // Mesmo se a chamada falhar, limpamos a sessao local.
    }
    await _tokenStorage.limpar();
  }
}
