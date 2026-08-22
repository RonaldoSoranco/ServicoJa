import 'package:flutter/material.dart';

/// Exibicao de uma nota (0 a 5) em estrelas, com suporte a meia estrela.
class Estrelas extends StatelessWidget {
  const Estrelas({super.key, required this.nota, this.tamanho = 18, this.cor});

  final double nota;
  final double tamanho;
  final Color? cor;

  @override
  Widget build(BuildContext context) {
    final corPreenchida = cor ?? Colors.amber;
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: List.generate(5, (i) {
        final diferenca = nota - i;
        IconData icone;
        if (diferenca >= 1) {
          icone = Icons.star_rounded;
        } else if (diferenca >= 0.5) {
          icone = Icons.star_half_rounded;
        } else {
          icone = Icons.star_border_rounded;
        }
        return Icon(icone, size: tamanho, color: corPreenchida);
      }),
    );
  }
}

/// Variante interativa (1 a 5) para o usuario escolher uma nota, usada ao avaliar.
class EstrelasInterativas extends StatelessWidget {
  const EstrelasInterativas({super.key, required this.nota, required this.aoSelecionar, this.tamanho = 36});

  final int nota;
  final ValueChanged<int> aoSelecionar;
  final double tamanho;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: List.generate(5, (i) {
        final valor = i + 1;
        return IconButton(
          padding: EdgeInsets.zero,
          constraints: const BoxConstraints(),
          icon: Icon(
            valor <= nota ? Icons.star_rounded : Icons.star_border_rounded,
            color: Colors.amber,
            size: tamanho,
          ),
          onPressed: () => aoSelecionar(valor),
        );
      }),
    );
  }
}
