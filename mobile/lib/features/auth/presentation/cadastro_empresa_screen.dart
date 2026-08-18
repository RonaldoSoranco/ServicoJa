import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../categorias/data/categoria_repository.dart';
import '../../categorias/models/categoria.dart';
import '../state/auth_controller.dart';

class CadastroEmpresaScreen extends StatefulWidget {
  const CadastroEmpresaScreen({super.key});

  @override
  State<CadastroEmpresaScreen> createState() => _CadastroEmpresaScreenState();
}

class _CadastroEmpresaScreenState extends State<CadastroEmpresaScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nomeResponsavelController = TextEditingController();
  final _emailController = TextEditingController();
  final _senhaController = TextEditingController();
  final _telefoneController = TextEditingController();
  final _nomeEmpresaController = TextEditingController();
  final _cidadeController = TextEditingController(text: 'Marau');
  final _ufController = TextEditingController(text: 'RS');
  final _cpfController = TextEditingController();

  late final Future<List<Categoria>> _categoriasFuture;
  int? _categoriaId;

  @override
  void initState() {
    super.initState();
    _categoriasFuture = CategoriaRepository().listarAtivas();
  }

  @override
  void dispose() {
    _nomeResponsavelController.dispose();
    _emailController.dispose();
    _senhaController.dispose();
    _telefoneController.dispose();
    _nomeEmpresaController.dispose();
    _cidadeController.dispose();
    _ufController.dispose();
    _cpfController.dispose();
    super.dispose();
  }

  Future<void> _cadastrar() async {
    if (!_formKey.currentState!.validate()) return;
    if (_categoriaId == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Selecione uma categoria.')));
      return;
    }
    final auth = context.read<AuthController>();
    final ok = await auth.cadastrarEmpresa(
      nomeResponsavel: _nomeResponsavelController.text.trim(),
      email: _emailController.text.trim(),
      senha: _senhaController.text,
      telefone: _telefoneController.text.trim(),
      nomeEmpresa: _nomeEmpresaController.text.trim(),
      categoriaId: _categoriaId!,
      cidade: _cidadeController.text.trim(),
      uf: _ufController.text.trim().toUpperCase(),
      cpf: _cpfController.text.trim(),
    );
    if (!mounted) return;
    if (ok) {
      Navigator.of(context).popUntil((route) => route.isFirst);
    } else {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(auth.erro ?? 'Nao foi possivel cadastrar.')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthController>();
    return Scaffold(
      appBar: AppBar(title: const Text('Cadastro de empresa')),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text('Seus dados', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _nomeResponsavelController,
                      decoration: const InputDecoration(labelText: 'Nome do responsavel'),
                      validator: (v) => (v == null || v.trim().isEmpty) ? 'Informe seu nome.' : null,
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _emailController,
                      keyboardType: TextInputType.emailAddress,
                      decoration: const InputDecoration(labelText: 'E-mail'),
                      validator: (v) => (v == null || !v.contains('@')) ? 'Informe um e-mail valido.' : null,
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _senhaController,
                      obscureText: true,
                      decoration: const InputDecoration(labelText: 'Senha', helperText: 'Minimo de 8 caracteres.'),
                      validator: (v) => (v == null || v.length < 8) ? 'A senha deve ter ao menos 8 caracteres.' : null,
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _telefoneController,
                      keyboardType: TextInputType.phone,
                      decoration: const InputDecoration(labelText: 'Telefone (opcional)'),
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _cpfController,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(labelText: 'CPF (opcional, so numeros)'),
                    ),
                    const SizedBox(height: 24),
                    Text('Dados da empresa', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _nomeEmpresaController,
                      decoration: const InputDecoration(labelText: 'Nome da empresa'),
                      validator: (v) => (v == null || v.trim().isEmpty) ? 'Informe o nome da empresa.' : null,
                    ),
                    const SizedBox(height: 16),
                    FutureBuilder<List<Categoria>>(
                      future: _categoriasFuture,
                      builder: (context, snapshot) {
                        if (snapshot.connectionState != ConnectionState.done) {
                          return const Padding(
                            padding: EdgeInsets.symmetric(vertical: 8),
                            child: LinearProgressIndicator(),
                          );
                        }
                        if (snapshot.hasError) {
                          return Text(
                            'Nao foi possivel carregar as categorias. Verifique sua conexao.',
                            style: TextStyle(color: Theme.of(context).colorScheme.error),
                          );
                        }
                        final categorias = snapshot.data ?? [];
                        return DropdownButtonFormField<int>(
                          initialValue: _categoriaId,
                          decoration: const InputDecoration(labelText: 'Categoria'),
                          items: categorias
                              .map((c) => DropdownMenuItem(value: c.id, child: Text(c.nome)))
                              .toList(),
                          onChanged: (v) => setState(() => _categoriaId = v),
                        );
                      },
                    ),
                    const SizedBox(height: 16),
                    Row(
                      children: [
                        Expanded(
                          flex: 2,
                          child: TextFormField(
                            controller: _cidadeController,
                            decoration: const InputDecoration(labelText: 'Cidade'),
                            validator: (v) => (v == null || v.trim().isEmpty) ? 'Informe a cidade.' : null,
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: TextFormField(
                            controller: _ufController,
                            maxLength: 2,
                            textCapitalization: TextCapitalization.characters,
                            decoration: const InputDecoration(labelText: 'UF', counterText: ''),
                            validator: (v) => (v == null || v.trim().length != 2) ? 'UF invalida.' : null,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    ElevatedButton(
                      onPressed: auth.carregando ? null : _cadastrar,
                      child: auth.carregando
                          ? const SizedBox(
                              height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                          : const Text('Criar conta da empresa'),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Sua empresa ficara pendente de aprovacao do administrador antes de aparecer nas buscas.',
                      textAlign: TextAlign.center,
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Colors.black54),
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
