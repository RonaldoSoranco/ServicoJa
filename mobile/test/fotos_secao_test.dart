import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:servicoja_app/features/empresas/presentation/fotos_secao.dart';

void main() {
  testWidgets('Mostra bloqueio quando a empresa nao e Premium', (WidgetTester tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: FotosSecao(
            premiumAtivo: false,
            fotos: const [],
            aoAdicionar: (_, _, _) async {},
            aoRemover: (_) async {},
          ),
        ),
      ),
    );

    expect(find.text('Fotos (Premium)'), findsOneWidget);
    expect(find.byIcon(Icons.lock_outline), findsOneWidget);
    expect(find.text('Adicionar'), findsNothing);
  });

  testWidgets('Mostra botao de adicionar quando a empresa e Premium', (WidgetTester tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: FotosSecao(
            premiumAtivo: true,
            fotos: const [],
            aoAdicionar: (_, _, _) async {},
            aoRemover: (_) async {},
          ),
        ),
      ),
    );

    expect(find.text('Adicionar'), findsOneWidget);
    expect(find.text('Nenhuma foto cadastrada.'), findsOneWidget);
    expect(find.text('Fotos (Premium)'), findsNothing);
  });
}
