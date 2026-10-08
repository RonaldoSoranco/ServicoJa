import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/marca.dart';
import '../../perfil/presentation/esqueci_senha_screen.dart';
import '../state/auth_controller.dart';
import 'escolha_perfil_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final _senhaController = TextEditingController();
  bool _senhaVisivel = false;

  @override
  void dispose() {
    _emailController.dispose();
    _senhaController.dispose();
    super.dispose();
  }

  Future<void> _entrar() async {
    if (!_formKey.currentState!.validate()) return;
    final auth = context.read<AuthController>();
    final ok = await auth.login(email: _emailController.text.trim(), senha: _senhaController.text);
    if (!ok && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(auth.erro ?? 'Nao foi possivel entrar.')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthController>();
    return Scaffold(
      body: Stack(
        children: [
          Container(height: 320, decoration: const BoxDecoration(gradient: AppCores.gradienteMarca)),
          SafeArea(
            child: Center(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
                child: ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 420),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      const _Marca(),
                      const SizedBox(height: 28),
                      Card(
                        elevation: 8,
                        shadowColor: Colors.black26,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                        child: Padding(
                          padding: const EdgeInsets.fromLTRB(22, 26, 22, 18),
                          child: Form(
                            key: _formKey,
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.stretch,
                              children: [
                                Text('Entrar',
                                    style: Theme.of(context)
                                        .textTheme
                                        .headlineSmall
                                        ?.copyWith(fontWeight: FontWeight.w800)),
                                const SizedBox(height: 4),
                                const Text(
                                  'Encontre e avalie quem resolve em Marau e região.',
                                  style: TextStyle(color: AppCores.textoSecundario),
                                ),
                                const SizedBox(height: 22),
                                TextFormField(
                                  controller: _emailController,
                                  keyboardType: TextInputType.emailAddress,
                                  textInputAction: TextInputAction.next,
                                  decoration: const InputDecoration(
                                    labelText: 'E-mail',
                                    prefixIcon: Icon(Icons.mail_outline_rounded),
                                  ),
                                  validator: (v) => (v == null || !v.contains('@')) ? 'Informe um e-mail válido.' : null,
                                ),
                                const SizedBox(height: 14),
                                TextFormField(
                                  controller: _senhaController,
                                  obscureText: !_senhaVisivel,
                                  textInputAction: TextInputAction.done,
                                  onFieldSubmitted: (_) => _entrar(),
                                  decoration: InputDecoration(
                                    labelText: 'Senha',
                                    prefixIcon: const Icon(Icons.lock_outline_rounded),
                                    suffixIcon: IconButton(
                                      icon: Icon(_senhaVisivel ? Icons.visibility_off_rounded : Icons.visibility_rounded),
                                      onPressed: () => setState(() => _senhaVisivel = !_senhaVisivel),
                                    ),
                                  ),
                                  validator: (v) => (v == null || v.isEmpty) ? 'Informe sua senha.' : null,
                                ),
                                Align(
                                  alignment: Alignment.centerRight,
                                  child: TextButton(
                                    onPressed: auth.carregando
                                        ? null
                                        : () => Navigator.of(context)
                                            .push(MaterialPageRoute(builder: (_) => const EsqueciSenhaScreen())),
                                    child: const Text('Esqueci minha senha'),
                                  ),
                                ),
                                const SizedBox(height: 6),
                                ElevatedButton(
                                  onPressed: auth.carregando ? null : _entrar,
                                  child: auth.carregando
                                      ? const SizedBox(
                                          height: 20,
                                          width: 20,
                                          child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                                      : const Text('Entrar'),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(height: 16),
                      TextButton(
                        onPressed: auth.carregando
                            ? null
                            : () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => const EscolhaPerfilScreen())),
                        child: const Text.rich(TextSpan(
                          text: 'Ainda não tem conta? ',
                          style: TextStyle(color: AppCores.textoSecundario, fontWeight: FontWeight.w500),
                          children: [TextSpan(text: 'Cadastre-se', style: TextStyle(color: AppCores.laranjaEscuro, fontWeight: FontWeight.w800))],
                        )),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _Marca extends StatelessWidget {
  const _Marca();

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        const LogoServicoJa(tamanho: 76, invertido: true),
        const SizedBox(height: 14),
        Text(
          'Serviço Já',
          style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                color: Colors.white,
                fontWeight: FontWeight.w800,
                letterSpacing: -0.6,
              ),
        ),
        const SizedBox(height: 2),
        Text(
          'Quem resolve, perto de você.',
          style: TextStyle(color: Colors.white.withValues(alpha: 0.9), fontWeight: FontWeight.w600),
        ),
      ],
    );
  }
}
