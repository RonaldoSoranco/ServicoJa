import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

import 'package:servicoja_app/core/network/api_client.dart';

void main() {
  late http.Request enviada;

  ApiClient clienteQueResponde(String corpo) {
    return ApiClient(
      client: MockClient((request) async {
        enviada = request;
        return http.Response(corpo, 200);
      }),
    );
  }

  test('POST envia o corpo como JSON', () async {
    await clienteQueResponde('{"ok":true}').post('/api/auth/login', corpo: {'email': 'a@b.com', 'senha': 'x'});

    expect(enviada.method, 'POST');
    expect(enviada.headers['content-type'], startsWith('application/json'));
    expect(jsonDecode(enviada.body), {'email': 'a@b.com', 'senha': 'x'});
  });

  test('PUT envia o corpo como JSON', () async {
    await clienteQueResponde('{"ok":true}').put('/api/empresas/1', corpo: {'nome': 'Empresa'});

    expect(enviada.method, 'PUT');
    expect(enviada.headers['content-type'], startsWith('application/json'));
  });

  test('GET sem corpo nao declara Content-Type', () async {
    await clienteQueResponde('[]').get('/api/categorias');

    expect(enviada.headers.containsKey('content-type'), isFalse);
  });
}
