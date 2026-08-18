import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class TokenStorage {
  TokenStorage() : _storage = const FlutterSecureStorage();

  final FlutterSecureStorage _storage;

  static const _keyAccess = 'token_acesso';
  static const _keyRefresh = 'token_refresh';

  Future<void> salvar({required String tokenAcesso, required String tokenRefresh}) async {
    await _storage.write(key: _keyAccess, value: tokenAcesso);
    await _storage.write(key: _keyRefresh, value: tokenRefresh);
  }

  Future<String?> lerTokenAcesso() => _storage.read(key: _keyAccess);

  Future<String?> lerTokenRefresh() => _storage.read(key: _keyRefresh);

  Future<void> limpar() async {
    await _storage.delete(key: _keyAccess);
    await _storage.delete(key: _keyRefresh);
  }
}
