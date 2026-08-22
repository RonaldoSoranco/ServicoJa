import 'package:flutter/material.dart';

/// Estado vazio reutilizavel (lista sem itens, busca sem resultado, etc).
class EstadoVazio extends StatelessWidget {
  const EstadoVazio({
    super.key,
    required this.mensagem,
    this.icone = Icons.inbox_outlined,
    this.acao,
    this.rotuloAcao,
  });

  final String mensagem;
  final IconData icone;
  final VoidCallback? acao;
  final String? rotuloAcao;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icone, size: 56, color: Colors.black26),
            const SizedBox(height: 16),
            Text(mensagem, textAlign: TextAlign.center, style: const TextStyle(color: Colors.black54)),
            if (acao != null) ...[
              const SizedBox(height: 16),
              OutlinedButton(onPressed: acao, child: Text(rotuloAcao ?? 'Limpar filtros')),
            ],
          ],
        ),
      ),
    );
  }
}
