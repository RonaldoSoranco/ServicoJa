import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../auth/models/usuario.dart';
import '../../auth/state/auth_controller.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  String _rotuloPerfil(Perfil perfil) {
    switch (perfil) {
      case Perfil.cliente:
        return 'Cliente';
      case Perfil.empresa:
        return 'Empresa';
      case Perfil.admin:
        return 'Administrador';
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthController>();
    final usuario = auth.usuario;
    return Scaffold(
      appBar: AppBar(
        title: const Text('Servico Ja'),
        actions: [
          IconButton(
            tooltip: 'Sair',
            icon: const Icon(Icons.logout),
            onPressed: auth.carregando ? null : () => context.read<AuthController>().logout(),
          ),
        ],
      ),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.check_circle, color: Colors.green, size: 56),
              const SizedBox(height: 16),
              Text(
                'Bem-vindo, ${usuario?.nome ?? ''}!',
                style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 8),
              if (usuario != null)
                Chip(label: Text(_rotuloPerfil(usuario.perfil))),
              const SizedBox(height: 24),
              const Text(
                'Login conectado ao backend do Servico Ja.\nAs proximas telas (busca, perfil de empresa, avaliacoes) '
                'entram aqui.',
                textAlign: TextAlign.center,
                style: TextStyle(color: Colors.black54),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

