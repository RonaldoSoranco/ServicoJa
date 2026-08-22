import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../auth/models/usuario.dart';
import '../../auth/state/auth_controller.dart';
import '../../avaliacoes/presentation/minhas_avaliacoes_screen.dart';
import 'alterar_senha_screen.dart';
import 'editar_perfil_screen.dart';

class PerfilScreen extends StatelessWidget {
  const PerfilScreen({super.key});

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

  Future<void> _confirmarSaida(BuildContext context) async {
    final confirmar = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Sair'),
        content: const Text('Tem certeza que deseja sair da sua conta?'),
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
    final auth = context.watch<AuthController>();
    final usuario = auth.usuario;
    return Scaffold(
      appBar: AppBar(title: const Text('Perfil')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: Theme.of(context).colorScheme.primary.withValues(alpha: 0.06),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Column(
              children: [
                CircleAvatar(
                  radius: 32,
                  backgroundColor: Theme.of(context).colorScheme.primary,
                  child: Text(
                    (usuario?.nome.isNotEmpty ?? false) ? usuario!.nome[0].toUpperCase() : '?',
                    style: const TextStyle(fontSize: 24, color: Colors.white, fontWeight: FontWeight.bold),
                  ),
                ),
                const SizedBox(height: 12),
                Text(usuario?.nome ?? '', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 17)),
                const SizedBox(height: 4),
                Text(usuario?.email ?? '', style: const TextStyle(color: Colors.black54)),
                const SizedBox(height: 8),
                if (usuario != null) Chip(label: Text(_rotuloPerfil(usuario.perfil))),
              ],
            ),
          ),
          const SizedBox(height: 16),
          _item(
            context,
            icone: Icons.edit_outlined,
            titulo: 'Editar perfil',
            onTap: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => const EditarPerfilScreen())),
          ),
          _item(
            context,
            icone: Icons.lock_outline,
            titulo: 'Alterar senha',
            onTap: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => const AlterarSenhaScreen())),
          ),
          if (usuario?.perfil == Perfil.cliente)
            _item(
              context,
              icone: Icons.rate_review_outlined,
              titulo: 'Minhas avaliacoes',
              onTap: () =>
                  Navigator.of(context).push(MaterialPageRoute(builder: (_) => const MinhasAvaliacoesScreen())),
            ),
          const SizedBox(height: 8),
          _item(
            context,
            icone: Icons.logout,
            titulo: 'Sair',
            cor: Theme.of(context).colorScheme.error,
            onTap: auth.carregando ? null : () => _confirmarSaida(context),
          ),
        ],
      ),
    );
  }

  Widget _item(
    BuildContext context, {
    required IconData icone,
    required String titulo,
    required VoidCallback? onTap,
    Color? cor,
  }) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12), side: BorderSide(color: Colors.grey.shade200)),
      child: ListTile(
        leading: Icon(icone, color: cor),
        title: Text(titulo, style: TextStyle(color: cor)),
        trailing: const Icon(Icons.chevron_right, color: Colors.black38),
        onTap: onTap,
      ),
    );
  }
}
