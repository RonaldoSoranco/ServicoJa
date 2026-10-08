import 'package:flutter/material.dart';

import '../../../core/theme/app_theme.dart';
import '../models/horario.dart';

/// Edição dos horários de funcionamento: cada dia pode estar fechado ou ter até 3 intervalos
/// (ex.: 08:00–12:00 e 13:30–18:00).
class EditorHorarios extends StatelessWidget {
  const EditorHorarios({super.key, required this.horarios, required this.aoAlterar});

  final List<Horario> horarios;
  final ValueChanged<List<Horario>> aoAlterar;

  static const _maximoIntervalosPorDia = 3;
  static const _padrao = (abre: TimeOfDay(hour: 8, minute: 0), fecha: TimeOfDay(hour: 18, minute: 0));

  List<Horario> _doDia(int dia) => horarios.where((h) => h.diaSemana == dia).toList()
    ..sort((a, b) => (a.abre.hour * 60 + a.abre.minute).compareTo(b.abre.hour * 60 + b.abre.minute));

  void _alternarDia(int dia, bool aberto) {
    final outros = horarios.where((h) => h.diaSemana != dia).toList();
    aoAlterar(aberto ? [...outros, Horario(diaSemana: dia, abre: _padrao.abre, fecha: _padrao.fecha)] : outros);
  }

  void _adicionarIntervalo(int dia) {
    final doDia = _doDia(dia);
    final ultimoFecha = doDia.isEmpty ? _padrao.abre : doDia.last.fecha;
    final abre = TimeOfDay(hour: (ultimoFecha.hour + 1).clamp(0, 22), minute: ultimoFecha.minute);
    final fecha = TimeOfDay(hour: (abre.hour + 4).clamp(abre.hour + 1, 23), minute: abre.minute);
    aoAlterar([...horarios, Horario(diaSemana: dia, abre: abre, fecha: fecha)]);
  }

  void _remover(Horario horario) => aoAlterar(horarios.where((h) => !identical(h, horario)).toList());

  Future<void> _editar(BuildContext context, Horario horario) async {
    final abre = await showTimePicker(context: context, initialTime: horario.abre, helpText: 'Abre às');
    if (abre == null || !context.mounted) return;
    final fecha = await showTimePicker(context: context, initialTime: horario.fecha, helpText: 'Fecha às');
    if (fecha == null || !context.mounted) return;
    final novo = Horario(diaSemana: horario.diaSemana, abre: abre, fecha: fecha);
    if (!novo.valido) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('O horário de fechamento precisa ser depois do de abertura.')),
      );
      return;
    }
    aoAlterar(horarios.map((h) => identical(h, horario) ? novo : h).toList());
  }

  void _copiarSegundaParaDiasUteis() {
    final segunda = _doDia(1);
    final semDiasUteis = horarios.where((h) => h.diaSemana == 1 || h.diaSemana > 5).toList();
    aoAlterar([
      ...semDiasUteis,
      for (var dia = 2; dia <= 5; dia++) ...segunda.map((h) => h.copiarPara(dia)),
    ]);
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        for (var dia = 1; dia <= 7; dia++) _linhaDia(context, dia),
        if (_doDia(1).isNotEmpty)
          Align(
            alignment: Alignment.centerLeft,
            child: TextButton.icon(
              onPressed: _copiarSegundaParaDiasUteis,
              icon: const Icon(Icons.copy_all_rounded, size: 18),
              label: const Text('Repetir o horário de segunda até sexta'),
            ),
          ),
      ],
    );
  }

  Widget _linhaDia(BuildContext context, int dia) {
    final doDia = _doDia(dia);
    final aberto = doDia.isNotEmpty;
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          SizedBox(
            width: 44,
            child: Text(nomesDiasCurtos[dia - 1], style: const TextStyle(fontWeight: FontWeight.w700)),
          ),
          Switch(value: aberto, onChanged: (valor) => _alternarDia(dia, valor)),
          const SizedBox(width: 6),
          Expanded(
            child: aberto
                ? Wrap(
                    spacing: 6,
                    runSpacing: 6,
                    crossAxisAlignment: WrapCrossAlignment.center,
                    children: [
                      ...doDia.map((h) => InputChip(
                            label: Text('${formatarHora(h.abre)} – ${formatarHora(h.fecha)}'),
                            onPressed: () => _editar(context, h),
                            onDeleted: doDia.length > 1 ? () => _remover(h) : null,
                            deleteIcon: const Icon(Icons.close_rounded, size: 16),
                          )),
                      if (doDia.length < _maximoIntervalosPorDia)
                        IconButton(
                          tooltip: 'Adicionar intervalo',
                          visualDensity: VisualDensity.compact,
                          onPressed: () => _adicionarIntervalo(dia),
                          icon: const Icon(Icons.add_circle_outline_rounded, color: AppCores.laranja),
                        ),
                    ],
                  )
                : const Text('Fechado', style: TextStyle(color: AppCores.textoSecundario)),
          ),
        ],
      ),
    );
  }
}
