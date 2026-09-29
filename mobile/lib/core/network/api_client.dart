import 'dart:convert';

import 'package:http/http.dart' as http;

import '../config/app_config.dart';
import '../storage/token_storage.dart';
import 'api_exception.dart';

class ApiClient {
  ApiClient({http.Client? client, TokenStorage? tokenStorage})
      : _client = client ?? http.Client(),
        _tokenStorage = tokenStorage ?? TokenStorage();

  final http.Client _client;
  final TokenStorage _tokenStorage;

  Uri _uri(String caminho, [Map<String, String>? query]) {
    return Uri.parse('${AppConfig.apiBaseUrl}$caminho').replace(
      queryParameters: query?.isEmpty ?? true ? null : query,
    );
  }

  Future<dynamic> get(String caminho, {Map<String, String>? query, bool autenticado = false}) {
    return _enviar('GET', _uri(caminho, query), autenticado: autenticado);
  }

  Future<dynamic> post(String caminho, {Object? corpo, bool autenticado = false}) {
    return _enviar('POST', _uri(caminho), corpo: corpo, autenticado: autenticado);
  }

  Future<dynamic> put(String caminho, {Object? corpo, bool autenticado = false}) {
    return _enviar('PUT', _uri(caminho), corpo: corpo, autenticado: autenticado);
  }

  Future<dynamic> patch(String caminho, {Object? corpo, bool autenticado = false}) {
    return _enviar('PATCH', _uri(caminho), corpo: corpo, autenticado: autenticado);
  }

  Future<dynamic> delete(String caminho, {bool autenticado = false}) {
    return _enviar('DELETE', _uri(caminho), autenticado: autenticado);
  }

  /// Envia um arquivo via `multipart/form-data` (ex.: foto/logo da empresa).
  ///
  /// [campos] vira campos de texto adicionais do formulario (ex.: descricao, ordem).
  Future<dynamic> enviarArquivo(
    String caminho, {
    required List<int> bytes,
    required String nomeArquivo,
    required String campoArquivo,
    Map<String, String>? campos,
    bool autenticado = false,
  }) {
    return _enviarComRetry(
      autenticado: autenticado,
      construir: (headers) {
        final request = http.MultipartRequest('POST', _uri(caminho))..headers.addAll(headers);
        if (campos != null) request.fields.addAll(campos);
        request.files.add(http.MultipartFile.fromBytes(campoArquivo, bytes, filename: nomeArquivo));
        return request;
      },
    );
  }

  Future<dynamic> _enviar(
    String metodo,
    Uri uri, {
    Object? corpo,
    bool autenticado = false,
  }) {
    return _enviarComRetry(
      autenticado: autenticado,
      construir: (headers) {
        final request = http.Request(metodo, uri)..headers.addAll(headers);
        if (corpo != null) request.body = jsonEncode(corpo);
        return request;
      },
    );
  }

  Future<dynamic> _enviarComRetry({
    required http.BaseRequest Function(Map<String, String> headers) construir,
    bool autenticado = false,
    bool tentandoNovamenteAposRefresh = false,
  }) async {
    final headers = {'Accept': 'application/json'};
    if (autenticado) {
      final token = await _tokenStorage.lerTokenAcesso();
      if (token != null) headers['Authorization'] = 'Bearer $token';
    }

    http.Response resposta;
    try {
      final request = construir(headers);
      if (request is http.Request) {
        request.headers.putIfAbsent('Content-Type', () => 'application/json');
      }
      final streamed = await _client.send(request);
      resposta = await http.Response.fromStream(streamed);
    } on Exception {
      throw ApiException.semConexao();
    }

    if (resposta.statusCode == 401 && autenticado && !tentandoNovamenteAposRefresh) {
      final renovou = await _tentarRenovarToken();
      if (renovou) {
        return _enviarComRetry(construir: construir, autenticado: autenticado, tentandoNovamenteAposRefresh: true);
      }
    }

    if (resposta.statusCode >= 200 && resposta.statusCode < 300) {
      if (resposta.body.isEmpty) return null;
      return jsonDecode(utf8.decode(resposta.bodyBytes));
    }

    Map<String, dynamic>? corpoErro;
    try {
      corpoErro = jsonDecode(utf8.decode(resposta.bodyBytes)) as Map<String, dynamic>;
    } catch (_) {
      corpoErro = null;
    }
    throw ApiException.fromBody(resposta.statusCode, corpoErro);
  }

  Future<bool> _tentarRenovarToken() async {
    final tokenRefresh = await _tokenStorage.lerTokenRefresh();
    if (tokenRefresh == null) return false;
    try {
      final resposta = await _client.post(
        _uri('/api/auth/refresh'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'tokenRefresh': tokenRefresh}),
      );
      if (resposta.statusCode != 200) {
        await _tokenStorage.limpar();
        return false;
      }
      final json = jsonDecode(utf8.decode(resposta.bodyBytes)) as Map<String, dynamic>;
      await _tokenStorage.salvar(
        tokenAcesso: json['tokenAcesso'] as String,
        tokenRefresh: json['tokenRefresh'] as String,
      );
      return true;
    } catch (_) {
      return false;
    }
  }
}
