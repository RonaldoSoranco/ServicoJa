import 'package:flutter/material.dart';

import 'secao_bloqueada_premium.dart';

/// Secao "Destaque" da tela de edicao de empresa (Fase 8).
///
/// So fica habilitada se `premiumAtivo && aprovada`.
class DestaqueSecao extends StatelessWidget {
  const DestaqueSecao({
    super.key,
    required this.premiumAtivo,
    required this.aprovada,
    required this.destaque,
    required this.processando,
    required this.aoAlternar,
  });

  final bool premiumAtivo;
  final bool aprovada;
  final bool destaque;
  final bool processando;
  final ValueChanged<bool> aoAlternar;

  @override
  Widget build(BuildContext context) {
    final habilitado = premiumAtivo && aprovada;
    if (!habilitado) {
      return SecaoBloqueadaPremium(
        titulo: 'Destaque (Premium)',
        mensagem: !premiumAtivo
            ? 'Destacar a empresa nas buscas e exclusivo para empresas Premium.'
            : 'Sua empresa precisa estar aprovada para poder ser destacada.',
      );
    }
    return Row(
      children: [
        const Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Destacar empresa', style: TextStyle(fontWeight: FontWeight.bold)),
              SizedBox(height: 4),
              Text('Empresas destacadas ganham um selo na lista de busca.', style: TextStyle(color: Colors.black54, fontSize: 12)),
            ],
          ),
        ),
        if (processando)
          const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2))
        else
          Switch(value: destaque, onChanged: aoAlternar),
      ],
    );
  }
}
