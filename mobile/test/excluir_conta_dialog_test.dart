import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:servicoja_app/features/perfil/presentation/excluir_conta_dialog.dart';

void main() {
  Future<void> abrirDialogo(
    WidgetTester tester, {
    bool possuiEmpresas = false,
    required Future<String?> Function(String senha) aoConfirmar,
    void Function(bool? resultado)? aoFechar,
  }) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Builder(
          builder: (context) => Scaffold(
            body: Center(
              child: ElevatedButton(
                onPressed: () async {
                  final resultado = await showDialog<bool>(
                    context: context,
                    builder: (_) => ExcluirContaDialog(possuiEmpresas: possuiEmpresas, aoConfirmar: aoConfirmar),
                  );
                  aoFechar?.call(resultado);
                },
                child: const Text('Abrir'),
              ),
            ),
          ),
        ),
      ),
    );
    await tester.tap(find.text('Abrir'));
    await tester.pumpAndSettle();
  }

  Finder botaoExcluir() => find.widgetWithText(FilledButton, 'Excluir conta');

  testWidgets('Exige a senha antes de excluir', (WidgetTester tester) async {
    var chamadas = 0;
    await abrirDialogo(tester, aoConfirmar: (_) async {
      chamadas++;
      return null;
    });

    await tester.tap(botaoExcluir());
    await tester.pump();

    expect(find.text('Informe sua senha.'), findsOneWidget);
    expect(chamadas, 0);
  });

  testWidgets('Avisa que as empresas saem da plataforma', (WidgetTester tester) async {
    await abrirDialogo(tester, possuiEmpresas: true, aoConfirmar: (_) async => null);

    expect(find.textContaining('Suas empresas sairao da plataforma'), findsOneWidget);
  });

  testWidgets('Mostra o erro devolvido e mantem o dialogo aberto', (WidgetTester tester) async {
    await abrirDialogo(tester, aoConfirmar: (_) async => 'Senha incorreta.');

    await tester.enterText(find.byType(TextFormField), 'senha-errada');
    await tester.tap(botaoExcluir());
    await tester.pumpAndSettle();

    expect(find.text('Senha incorreta.'), findsOneWidget);
    expect(find.byType(ExcluirContaDialog), findsOneWidget);
  });

  testWidgets('Fecha o dialogo quando a exclusao da certo', (WidgetTester tester) async {
    bool? resultado;
    String? senhaRecebida;
    await abrirDialogo(
      tester,
      aoConfirmar: (senha) async {
        senhaRecebida = senha;
        return null;
      },
      aoFechar: (r) => resultado = r,
    );

    await tester.enterText(find.byType(TextFormField), 'minha-senha');
    await tester.tap(botaoExcluir());
    await tester.pumpAndSettle();

    expect(senhaRecebida, 'minha-senha');
    expect(resultado, isTrue);
    expect(find.byType(ExcluirContaDialog), findsNothing);
  });
}
