import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'core/theme/app_theme.dart';
import 'features/auth/presentation/auth_gate.dart';
import 'features/auth/state/auth_controller.dart';
import 'features/notificacoes/state/notificacao_badge_controller.dart';

class ServicoJaApp extends StatelessWidget {
  const ServicoJaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthController()),
        ChangeNotifierProvider(create: (_) => NotificacaoBadgeController()),
      ],
      child: MaterialApp(
        title: 'Servico Ja',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.claro(),
        home: const AuthGate(),
      ),
    );
  }
}
