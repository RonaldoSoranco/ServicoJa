import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../core/network/api_exception.dart';
import '../../auth/data/auth_repository.dart';

/// Segunda etapa do fluxo "esqueci minha senha": o usuario cola o token
/// recebido por e-mail e define uma nova senha.
class RedefinirSenhaScreen extends StatefulWidget {
  const RedefinirSenhaScreen({super.key});

  @override
  State<RedefinirSenhaScreen> createState() => _RedefinirSenhaScreenState();
}

class _RedefinirSenhaScreenState extends State<RedefinirSenhaScreen> {
  final _formKey = GlobalKey<FormState>();
  final _repositorio = AuthRepository();
  final _tokenController = TextEditingController();
  final _novaSenhaController = TextEditingController();
  bool _carregando = false;
  String? _erro;

  @override
  void dispose() {
    _tokenController.dispose();
    _novaSenhaController.dispose();
    super.dispose();
  }

  Future<void> _colarToken() async {
    final dados = await Clipboard.getData('text/plain');
    if (dados?.text != null) {
      _tokenController.text = dados!.text!.trim();
    }
  }

  Future<void> _redefinir() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      _carregando = true;
      _erro = null;
    });
    try {
      await _repositorio.redefinirSenha(token: _tokenController.text.trim(), novaSenha: _novaSenhaController.text);
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(const SnackBar(content: Text('Senha redefinida com sucesso. Faca login novamente.')));
      Navigator.of(context).popUntil((route) => route.isFirst);
    } on ApiException catch (e) {
      setState(() => _erro = e.toString());
    } catch (_) {
      setState(() => _erro = 'Ocorreu um erro inesperado. Tente novamente.');
    } finally {
      if (mounted) setState(() => _carregando = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Redefinir senha')),
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
                      'Cole abaixo o token que voce recebeu por e-mail e defina sua nova senha.',
                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Colors.black54),
                    ),
                    const SizedBox(height: 20),
                    TextFormField(
                      controller: _tokenController,
                      maxLines: 3,
                      decoration: InputDecoration(
                        labelText: 'Token de redefinicao',
                        suffixIcon: IconButton(
                          tooltip: 'Colar da area de transferencia',
                          icon: const Icon(Icons.content_paste),
                          onPressed: _colarToken,
                        ),
                      ),
                      validator: (v) => (v == null || v.trim().isEmpty) ? 'Informe o token recebido.' : null,
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _novaSenhaController,
                      obscureText: true,
                      decoration: const InputDecoration(labelText: 'Nova senha', helperText: 'Minimo de 8 caracteres.'),
                      validator: (v) => (v == null || v.length < 8) ? 'A senha deve ter ao menos 8 caracteres.' : null,
                    ),
                    if (_erro != null) ...[
                      const SizedBox(height: 8),
                      Text(_erro!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
                    ],
                    const SizedBox(height: 24),
                    ElevatedButton(
                      onPressed: _carregando ? null : _redefinir,
                      child: _carregando
                          ? const SizedBox(
                              height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                          : const Text('Redefinir senha'),
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
