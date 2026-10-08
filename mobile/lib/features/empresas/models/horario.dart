import 'package:flutter/material.dart';

/// Intervalo de atendimento em um dia da semana (1 = segunda ... 7 = domingo, como `DateTime.weekday`).
class Horario {
  const Horario({required this.diaSemana, required this.abre, required this.fecha});

  factory Horario.fromJson(Map<String, dynamic> json) {
    return Horario(
      diaSemana: (json['diaSemana'] as num).toInt(),
      abre: _lerHora(json['abre'] as String),
      fecha: _lerHora(json['fecha'] as String),
    );
  }

  final int diaSemana;
  final TimeOfDay abre;
  final TimeOfDay fecha;

  Map<String, dynamic> toJson() => {'diaSemana': diaSemana, 'abre': formatarHora(abre), 'fecha': formatarHora(fecha)};

  int get _abreEmMinutos => abre.hour * 60 + abre.minute;
  int get _fechaEmMinutos => fecha.hour * 60 + fecha.minute;

  bool get valido => _fechaEmMinutos > _abreEmMinutos;

  bool cobre(DateTime momento) {
    final minutos = momento.hour * 60 + momento.minute;
    return momento.weekday == diaSemana && minutos >= _abreEmMinutos && minutos < _fechaEmMinutos;
  }

  Horario copiarPara(int dia) => Horario(diaSemana: dia, abre: abre, fecha: fecha);

  static TimeOfDay _lerHora(String valor) {
    final partes = valor.split(':');
    return TimeOfDay(hour: int.parse(partes[0]), minute: int.parse(partes[1]));
  }
}

String formatarHora(TimeOfDay hora) =>
    '${hora.hour.toString().padLeft(2, '0')}:${hora.minute.toString().padLeft(2, '0')}';

const nomesDias = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo'];
const nomesDiasCurtos = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'];

String nomeDia(int diaSemana) => nomesDias[diaSemana - 1];

/// Situação de funcionamento para exibir ao cliente, ex.: "Aberto · fecha às 18:00".
class SituacaoFuncionamento {
  const SituacaoFuncionamento({required this.aberto, required this.detalhe});

  final bool aberto;
  final String detalhe;

  /// Retorna `null` quando a empresa não informou horários.
  static SituacaoFuncionamento? calcular(List<Horario> horarios, DateTime agora) {
    if (horarios.isEmpty) return null;
    final atual = horarios.where((h) => h.cobre(agora)).firstOrNull;
    if (atual != null) {
      return SituacaoFuncionamento(aberto: true, detalhe: 'fecha às ${formatarHora(atual.fecha)}');
    }
    final proxima = _proximaAbertura(horarios, agora);
    if (proxima == null) return const SituacaoFuncionamento(aberto: false, detalhe: '');
    final (diasAte, horario) = proxima;
    final quando = switch (diasAte) {
      0 => 'hoje',
      1 => 'amanhã',
      _ => nomeDia(horario.diaSemana).toLowerCase(),
    };
    return SituacaoFuncionamento(aberto: false, detalhe: 'abre $quando às ${formatarHora(horario.abre)}');
  }

  static (int, Horario)? _proximaAbertura(List<Horario> horarios, DateTime agora) {
    final minutosAgora = agora.hour * 60 + agora.minute;
    for (var diasAte = 0; diasAte <= 7; diasAte++) {
      final dia = (agora.weekday - 1 + diasAte) % 7 + 1;
      final candidatos = horarios
          .where((h) => h.diaSemana == dia && (diasAte > 0 || h.abre.hour * 60 + h.abre.minute > minutosAgora))
          .toList()
        ..sort((a, b) => (a.abre.hour * 60 + a.abre.minute).compareTo(b.abre.hour * 60 + b.abre.minute));
      if (candidatos.isNotEmpty) return (diasAte, candidatos.first);
    }
    return null;
  }
}
