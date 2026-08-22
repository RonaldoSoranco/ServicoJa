import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../auth/state/auth_controller.dart';

class AlterarSenhaScreen extends StatefulWidget {
  const AlterarSenhaScreen({super.key});

  @override
  State<AlterarSenhaScreen> createState() => _AlterarSenhaScreenState();
}

class _AlterarSenhaScreenState extends State<AlterarSenhaScreen> {
  final _formKey = GlobalKey<FormState>();
  final _senhaAtualController = TextEditingController();
  final _novaSenhaController = TextEditingController();
  final _confirmarSenhaController = TextEditingController();

  @override
  void dispose() {
    _senhaAtualController.dispose();
    _novaSenhaController.dispose();
    _confirmarSenhaController.dispose();
    super.dispose();
  }

  Future<void> _salvar() async {
    if (!_formKey.currentState!.validate()) return;
    final auth = context.read<AuthController>();
    final ok = await auth.alterarSenha(
      senhaAtual: _senhaAtualController.text,
      novaSenha: _novaSenhaController.text,
    );
    if (!mounted) return;
    if (ok) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Senha alterada com sucesso.')));
      Navigator.of(context).pop();
    } else {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(auth.erro ?? 'Nao foi possivel alterar a senha.')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthController>();
    return Scaffold(
      appBar: AppBar(title: const Text('Alterar senha')),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    TextFormField(
                      controller: _senhaAtualController,
                      obscureText: true,
                      decoration: const InputDecoration(labelText: 'Senha atual'),
                      validator: (v) => (v == null || v.isEmpty) ? 'Informe sua senha atual.' : null,
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _novaSenhaController,
                      obscureText: true,
                      decoration: const InputDecoration(labelText: 'Nova senha', helperText: 'Minimo de 8 caracteres.'),
                      validator: (v) => (v == null || v.length < 8) ? 'A senha deve ter ao menos 8 caracteres.' : null,
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _confirmarSenhaController,
                      obscureText: true,
                      decoration: const InputDecoration(labelText: 'Confirmar nova senha'),
                      validator: (v) =>
                          (v != _novaSenhaController.text) ? 'As senhas informadas nao coincidem.' : null,
                    ),
                    const SizedBox(height: 24),
                    ElevatedButton(
                      onPressed: auth.carregando ? null : _salvar,
                      child: auth.carregando
                          ? const SizedBox(
                              height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                          : const Text('Alterar senha'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
