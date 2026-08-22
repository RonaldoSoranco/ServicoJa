import 'package:flutter/material.dart';

/// Estado bloqueado exibido quando uma secao (fotos/portfolio/destaque) exige
/// assinatura Premium e a empresa ainda nao tem uma ativa.
class SecaoBloqueadaPremium extends StatelessWidget {
  const SecaoBloqueadaPremium({super.key, required this.titulo, required this.mensagem});

  final String titulo;
  final String mensagem;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(color: Colors.grey.shade100, borderRadius: BorderRadius.circular(12)),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Icon(Icons.lock_outline, color: Colors.black38),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(titulo, style: const TextStyle(fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                Text(mensagem, style: const TextStyle(color: Colors.black54, fontSize: 13)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
