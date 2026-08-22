import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../auth/state/auth_controller.dart';

/// Botao de "Sair" reutilizado no AppBar de todas as telas do AdminShell,
/// ja que o admin nao tem uma aba "Perfil" propria.
class BotaoSairAdmin extends StatelessWidget {
  const BotaoSairAdmin({super.key});

  Future<void> _confirmarSaida(BuildContext context) async {
    final confirmar = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Sair'),
        content: const Text('Tem certeza que deseja sair da sua conta de administrador?'),
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Cancelar')),
          FilledButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('Sair')),
        ],
      ),
    );
    if (confirmar == true && context.mounted) {
      await context.read<AuthController>().logout();
    }
  }

  @override
  Widget build(BuildContext context) {
    return IconButton(
      tooltip: 'Sair',
      icon: const Icon(Icons.logout),
      onPressed: () => _confirmarSaida(context),
    );
  }
}
