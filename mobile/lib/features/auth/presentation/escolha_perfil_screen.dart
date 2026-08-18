import 'package:flutter/material.dart';

import 'cadastro_cliente_screen.dart';
import 'cadastro_empresa_screen.dart';

class EscolhaPerfilScreen extends StatelessWidget {
  const EscolhaPerfilScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Criar conta')),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Text(
                    'Como voce quer usar o Servico Ja?',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 24),
                  _CartaoPerfil(
                    icone: Icons.person_outline,
                    titulo: 'Sou cliente',
                    descricao: 'Quero buscar e avaliar prestadores de servico.',
                    onTap: () => Navigator.of(context)
                        .push(MaterialPageRoute(builder: (_) => const CadastroClienteScreen())),
                  ),
                  const SizedBox(height: 16),
                  _CartaoPerfil(
                    icone: Icons.storefront_outlined,
                    titulo: 'Sou uma empresa',
                    descricao: 'Quero cadastrar meu negocio e ganhar visibilidade.',
                    onTap: () => Navigator.of(context)
                        .push(MaterialPageRoute(builder: (_) => const CadastroEmpresaScreen())),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _CartaoPerfil extends StatelessWidget {
  const _CartaoPerfil({required this.icone, required this.titulo, required this.descricao, required this.onTap});

  final IconData icone;
  final String titulo;
  final String descricao;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      color: Colors.white,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16), side: BorderSide(color: Colors.grey.shade200)),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Row(
            children: [
              CircleAvatar(
                radius: 26,
                backgroundColor: Theme.of(context).colorScheme.primary.withValues(alpha: 0.1),
                child: Icon(icone, color: Theme.of(context).colorScheme.primary),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(titulo, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                    const SizedBox(height: 4),
                    Text(descricao, style: const TextStyle(color: Colors.black54, fontSize: 13)),
                  ],
                ),
              ),
              const Icon(Icons.chevron_right, color: Colors.black38),
            ],
          ),
        ),
      ),
    );
  }
}
