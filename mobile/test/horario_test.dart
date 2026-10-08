import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:servicoja_app/features/empresas/models/horario.dart';

void main() {
  // 2026-10-07 e uma quarta-feira (weekday 3).
  DateTime quarta(int hora, [int minuto = 0]) => DateTime(2026, 10, 7, hora, minuto);

  final comercial = [
    for (var dia = 1; dia <= 5; dia++) ...[
      Horario(diaSemana: dia, abre: const TimeOfDay(hour: 8, minute: 0), fecha: const TimeOfDay(hour: 12, minute: 0)),
      Horario(diaSemana: dia, abre: const TimeOfDay(hour: 13, minute: 30), fecha: const TimeOfDay(hour: 18, minute: 0)),
    ],
  ];

  test('Aberto mostra quando fecha', () {
    final situacao = SituacaoFuncionamento.calcular(comercial, quarta(10))!;

    expect(situacao.aberto, isTrue);
    expect(situacao.detalhe, 'fecha às 12:00');
  });

  test('Na pausa do almoco mostra que abre hoje de novo', () {
    final situacao = SituacaoFuncionamento.calcular(comercial, quarta(12, 30))!;

    expect(situacao.aberto, isFalse);
    expect(situacao.detalhe, 'abre hoje às 13:30');
  });

  test('Depois do expediente mostra que abre amanha', () {
    expect(SituacaoFuncionamento.calcular(comercial, quarta(19))!.detalhe, 'abre amanhã às 08:00');
  });

  test('Na sexta a noite mostra que abre na segunda', () {
    final sexta = DateTime(2026, 10, 9, 20);

    expect(SituacaoFuncionamento.calcular(comercial, sexta)!.detalhe, 'abre segunda às 08:00');
  });

  test('Sem horarios cadastrados nao calcula situacao', () {
    expect(SituacaoFuncionamento.calcular(const [], quarta(10)), isNull);
  });

  test('Converte de e para o formato da API', () {
    final horario = Horario.fromJson({'diaSemana': 2, 'abre': '08:30', 'fecha': '17:45:00'});

    expect(horario.toJson(), {'diaSemana': 2, 'abre': '08:30', 'fecha': '17:45'});
  });
}
