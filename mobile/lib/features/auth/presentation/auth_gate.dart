import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../home/presentation/home_screen.dart';
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
    final status = context.watch<AuthController>().status;
    switch (status) {
      case StatusAuth.verificando:
        return const Scaffold(body: Center(child: CircularProgressIndicator()));
      case StatusAuth.autenticado:
        return const HomeScreen();
      case StatusAuth.naoAutenticado:
        return const LoginScreen();
    }
  }
}
