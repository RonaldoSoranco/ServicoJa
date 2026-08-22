import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../shell/presentation/admin_shell.dart';
import '../../shell/presentation/app_shell.dart';
import '../models/usuario.dart';
import '../state/auth_controller.dart';
import 'login_screen.dart';

class AuthGate extends StatefulWidget {
  const AuthGate({super.key});

  @override
  State<AuthGate> createState() => _AuthGateState();
}

class _AuthGateState extends State<AuthGate> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<AuthController>().verificarSessao();
    });
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthController>();
    switch (auth.status) {
      case StatusAuth.verificando:
        return const Scaffold(body: Center(child: CircularProgressIndicator()));
      case StatusAuth.autenticado:
        return auth.usuario?.perfil == Perfil.admin ? const AdminShell() : const AppShell();
      case StatusAuth.naoAutenticado:
        return const LoginScreen();
    }
  }
}
