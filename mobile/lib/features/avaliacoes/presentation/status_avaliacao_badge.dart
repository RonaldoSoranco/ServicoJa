import 'package:flutter/material.dart';

import '../models/avaliacao.dart';

class StatusAvaliacaoBadge extends StatelessWidget {
  const StatusAvaliacaoBadge({super.key, required this.status});

  final StatusAvaliacao status;

  @override
  Widget build(BuildContext context) {
    late final Color cor;
    late final String rotulo;
    switch (status) {
      case StatusAvaliacao.pendente:
        cor = Colors.orange;
        rotulo = 'Pendente';
        break;
      case StatusAvaliacao.aprovada:
        cor = Colors.green;
        rotulo = 'Aprovada';
        break;
      case StatusAvaliacao.rejeitada:
        cor = Colors.red;
        rotulo = 'Rejeitada';
        break;
    }
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(color: cor.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(8)),
      child: Text(rotulo, style: TextStyle(color: cor, fontSize: 11, fontWeight: FontWeight.bold)),
    );
  }
}
