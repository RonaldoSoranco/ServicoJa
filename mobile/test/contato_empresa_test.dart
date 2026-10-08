import 'package:flutter_test/flutter_test.dart';

import 'package:servicoja_app/features/empresas/data/contato_empresa.dart';

void main() {
  test('WhatsApp de numero com DDD recebe o 55 e a mensagem inicial', () {
    final uri = ContatoEmpresa.whatsapp('(54) 99999-1234', nomeEmpresa: 'Elétrica Silva')!;

    expect(uri.host, 'wa.me');
    expect(uri.path, '/5554999991234');
    expect(uri.queryParameters['text'], contains('Elétrica Silva'));
  });

  test('WhatsApp que ja tem DDI nao duplica o 55', () {
    expect(ContatoEmpresa.whatsapp('+55 54 3342-1001', nomeEmpresa: 'X')!.path, '/555433421001');
  });

  test('Sem numero nao monta link de WhatsApp nem de ligacao', () {
    expect(ContatoEmpresa.whatsapp(null, nomeEmpresa: 'X'), isNull);
    expect(ContatoEmpresa.whatsapp('', nomeEmpresa: 'X'), isNull);
    expect(ContatoEmpresa.telefone('12'), isNull);
  });

  test('Ligacao usa o esquema tel com os digitos', () {
    expect(ContatoEmpresa.telefone('(54) 3342-1001').toString(), 'tel:5433421001');
  });

  test('Como chegar abre a rota no Google Maps', () {
    final uri = ContatoEmpresa.comoChegar(-28.4489, -52.1992);

    expect(uri.host, 'www.google.com');
    expect(uri.queryParameters['destination'], '-28.4489,-52.1992');
  });
}
