import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

import 'package:servicoja_app/core/theme/app_theme.dart';
import 'package:servicoja_app/features/auth/presentation/login_screen.dart';
import 'package:servicoja_app/features/auth/state/auth_controller.dart';

void main() {
  testWidgets('Tela de login exibe os campos de e-mail e senha', (WidgetTester tester) async {
    await tester.pumpWidget(
      ChangeNotifierProvider(
        create: (_) => AuthController(),
        child: MaterialApp(theme: AppTheme.claro(), home: const LoginScreen()),
      ),
    );
    await tester.pump();

    expect(find.text('Entrar'), findsWidgets);
    expect(find.byType(TextFormField), findsNWidgets(2));
    expect(find.text('Ainda nao tem conta? Cadastre-se'), findsOneWidget);
  });
}
