import 'package:flutter/material.dart';

/// Estado de erro reutilizavel, com botao de tentar novamente.
class EstadoErro extends StatelessWidget {
  const EstadoErro({super.key, required this.mensagem, required this.aoTentarNovamente});

  final String mensagem;
  final VoidCallback aoTentarNovamente;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.error_outline, size: 56, color: Theme.of(context).colorScheme.error),
            const SizedBox(height: 16),
            Text(mensagem, textAlign: TextAlign.center, style: const TextStyle(color: Colors.black54)),
            const SizedBox(height: 16),
            ElevatedButton(onPressed: aoTentarNovamente, child: const Text('Tentar novamente')),
          ],
        ),
      ),
    );
  }
}
