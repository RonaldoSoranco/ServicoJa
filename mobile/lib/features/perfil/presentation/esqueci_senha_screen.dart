import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../auth/data/auth_repository.dart';
import 'redefinir_senha_screen.dart';

/// Primeira etapa do fluxo "esqueci minha senha": pede o e-mail cadastrado.
class EsqueciSenhaScreen extends StatefulWidget {
  const EsqueciSenhaScreen({super.key});

  @override
  State<EsqueciSenhaScreen> createState() => _EsqueciSenhaScreenState();
}

class _EsqueciSenhaScreenState extends State<EsqueciSenhaScreen> {
  final _formKey = GlobalKey<FormState>();
  final _repositorio = AuthRepository();
  final _emailController = TextEditingController();
  bool _carregando = false;
  String? _mensagem;

  @override
  void dispose() {
    _emailController.dispose();
    super.dispose();
  }

  Future<void> _enviar() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      _carregando = true;
      _mensagem = null;
    });
    try {
      final mensagem = await _repositorio.recuperarSenha(email: _emailController.text.trim());
      setState(() => _mensagem = mensagem);
    } on ApiException catch (e) {
      setState(() => _mensagem = e.toString());
    } catch (_) {
      setState(() => _mensagem = 'Ocorreu um erro inesperado. Tente novamente.');
    } finally {
      if (mounted) setState(() => _carregando = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Esqueci minha senha')),
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
                    Text(
                      'Informe seu e-mail cadastrado. Se ele existir na base, enviaremos um token de redefinicao de senha.',
                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Colors.black54),
                    ),
                    const SizedBox(height: 20),
                    TextFormField(
                      controller: _emailController,
                      keyboardType: TextInputType.emailAddress,
                      decoration: const InputDecoration(labelText: 'E-mail'),
                      validator: (v) => (v == null || !v.contains('@')) ? 'Informe um e-mail valido.' : null,
                    ),
                    if (_mensagem != null) ...[
                      const SizedBox(height: 12),
                      Text(_mensagem!, style: const TextStyle(color: Colors.black54)),
                    ],
                    const SizedBox(height: 24),
                    ElevatedButton(
                      onPressed: _carregando ? null : _enviar,
                      child: _carregando
                          ? const SizedBox(
                              height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                          : const Text('Enviar'),
                    ),
                    const SizedBox(height: 8),
                    TextButton(
                      onPressed: () =>
                          Navigator.of(context).push(MaterialPageRoute(builder: (_) => const RedefinirSenhaScreen())),
                      child: const Text('Ja tenho um token, quero redefinir a senha'),
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
