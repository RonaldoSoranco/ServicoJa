import 'package:flutter/material.dart';

import '../theme/app_theme.dart';

/// Etiqueta arredondada usada para "Aberto agora", "Premium", distância etc.
class Selo extends StatelessWidget {
  const Selo({super.key, required this.texto, required this.cor, this.icone, this.fundo});

  final String texto;
  final Color cor;
  final IconData? icone;
  final Color? fundo;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 4),
      decoration: BoxDecoration(
        color: fundo ?? cor.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(40),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (icone != null) ...[Icon(icone, size: 13, color: cor), const SizedBox(width: 4)],
          Text(texto, style: TextStyle(color: cor, fontSize: 11.5, fontWeight: FontWeight.w700)),
        ],
      ),
    );
  }
}

/// "Aberto agora" em verde ou "Fechado" em cinza; nada quando a empresa não informou horários.
class SeloFuncionamento extends StatelessWidget {
  const SeloFuncionamento({super.key, required this.aberto, required this.temHorarios, this.detalhe});

  final bool aberto;
  final bool temHorarios;
  final String? detalhe;

  @override
  Widget build(BuildContext context) {
    if (!temHorarios) return const SizedBox.shrink();
    final texto = aberto ? 'Aberto agora' : 'Fechado';
    final complemento = (detalhe == null || detalhe!.isEmpty) ? '' : ' · $detalhe';
    return Selo(
      texto: '$texto$complemento',
      cor: aberto ? AppCores.verde : AppCores.textoSecundario,
      fundo: aberto ? AppCores.verdeClaro : const Color(0xFFF1F2F4),
      icone: aberto ? Icons.circle : Icons.schedule_rounded,
    );
  }
}

String formatarDistancia(double km) {
  if (km < 1) return '${(km * 1000).round()} m';
  return '${km.toStringAsFixed(1).replaceAll('.', ',')} km';
}
