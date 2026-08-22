import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/estrelas.dart';
import '../data/avaliacao_repository.dart';
import '../models/avaliacao.dart';

/// Abre o dialog de avaliacao (estrelas + comentario) e retorna a [Avaliacao]
/// criada em caso de sucesso, ou `null` se o usuario cancelar.
Future<Avaliacao?> mostrarDialogoAvaliar(BuildContext context, {required int empresaId}) {
  return showDialog<Avaliacao>(
    context: context,
    builder: (_) => _AvaliarDialog(empresaId: empresaId),
  );
}

class _AvaliarDialog extends StatefulWidget {
  const _AvaliarDialog({required this.empresaId});

  final int empresaId;

  @override
  State<_AvaliarDialog> createState() => _AvaliarDialogState();
}

class _AvaliarDialogState extends State<_AvaliarDialog> {
  final _repositorio = AvaliacaoRepository();
  final _comentarioController = TextEditingController();
  int _nota = 0;
  bool _enviando = false;
  String? _erro;

  @override
  void dispose() {
    _comentarioController.dispose();
    super.dispose();
  }

  Future<void> _enviar() async {
    if (_nota == 0) {
      setState(() => _erro = 'Selecione uma nota de 1 a 5 estrelas.');
      return;
    }
    setState(() {
      _enviando = true;
      _erro = null;
    });
    try {
      final avaliacao = await _repositorio.avaliar(
        widget.empresaId,
        nota: _nota,
        comentario: _comentarioController.text.trim(),
      );
      if (mounted) Navigator.of(context).pop(avaliacao);
    } on ApiException catch (e) {
      setState(() => _erro = e.toString());
    } catch (_) {
      setState(() => _erro = 'Ocorreu um erro inesperado. Tente novamente.');
    } finally {
      if (mounted) setState(() => _enviando = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Avaliar empresa'),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            const Text('Sua nota', style: TextStyle(color: Colors.black54)),
            const SizedBox(height: 8),
            EstrelasInterativas(nota: _nota, aoSelecionar: (v) => setState(() => _nota = v)),
            const SizedBox(height: 16),
            TextField(
              controller: _comentarioController,
              maxLines: 4,
              maxLength: 2000,
              decoration: const InputDecoration(
                labelText: 'Comentario (opcional)',
                alignLabelWithHint: true,
              ),
            ),
            if (_erro != null) ...[
              const SizedBox(height: 4),
              Text(_erro!, style: TextStyle(color: Theme.of(context).colorScheme.error, fontSize: 13)),
            ],
            const SizedBox(height: 4),
            Text(
              'Sua avaliacao fica pendente ate ser aprovada por um administrador.',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Colors.black45),
            ),
          ],
        ),
      ),
      actions: [
        TextButton(onPressed: _enviando ? null : () => Navigator.of(context).pop(), child: const Text('Cancelar')),
        FilledButton(
          onPressed: _enviando ? null : _enviar,
          child: _enviando
              ? const SizedBox(height: 18, width: 18, child: CircularProgressIndicator(strokeWidth: 2))
              : const Text('Enviar'),
        ),
      ],
    );
  }
}
