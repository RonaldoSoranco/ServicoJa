import 'package:flutter/material.dart';

/// Confirmacao da exclusao de conta: explica o que sera apagado e exige a senha atual.
///
/// [aoConfirmar] recebe a senha digitada e devolve `null` em caso de sucesso ou a
/// mensagem de erro a ser exibida (ex.: senha incorreta).
class ExcluirContaDialog extends StatefulWidget {
  const ExcluirContaDialog({super.key, required this.possuiEmpresas, required this.aoConfirmar});

  final bool possuiEmpresas;
  final Future<String?> Function(String senha) aoConfirmar;

  @override
  State<ExcluirContaDialog> createState() => _ExcluirContaDialogState();
}

class _ExcluirContaDialogState extends State<ExcluirContaDialog> {
  final _formKey = GlobalKey<FormState>();
  final _senhaController = TextEditingController();
  bool _excluindo = false;
  String? _erro;

  @override
  void dispose() {
    _senhaController.dispose();
    super.dispose();
  }

  Future<void> _confirmar() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      _excluindo = true;
      _erro = null;
    });
    final erro = await widget.aoConfirmar(_senhaController.text);
    if (!mounted) return;
    if (erro == null) {
      Navigator.of(context).pop(true);
    } else {
      setState(() {
        _excluindo = false;
        _erro = erro;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final corErro = Theme.of(context).colorScheme.error;
    return AlertDialog(
      title: const Text('Excluir conta'),
      content: SingleChildScrollView(
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Esta acao e permanente. Seus dados pessoais, favoritos, avaliacoes e notificacoes '
                'serao apagados.',
              ),
              if (widget.possuiEmpresas) ...[
                const SizedBox(height: 8),
                const Text(
                  'Suas empresas sairao da plataforma e as assinaturas Premium ativas serao canceladas.',
                ),
              ],
              const SizedBox(height: 16),
              TextFormField(
                controller: _senhaController,
                obscureText: true,
                enabled: !_excluindo,
                decoration: const InputDecoration(labelText: 'Confirme sua senha'),
                validator: (v) => (v == null || v.isEmpty) ? 'Informe sua senha.' : null,
              ),
              if (_erro != null) ...[
                const SizedBox(height: 8),
                Text(_erro!, style: TextStyle(color: corErro)),
              ],
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: _excluindo ? null : () => Navigator.of(context).pop(false),
          child: const Text('Cancelar'),
        ),
        FilledButton(
          style: FilledButton.styleFrom(backgroundColor: corErro),
          onPressed: _excluindo ? null : _confirmar,
          child: _excluindo
              ? const SizedBox(
                  height: 18, width: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
              : const Text('Excluir conta'),
        ),
      ],
    );
  }
}
