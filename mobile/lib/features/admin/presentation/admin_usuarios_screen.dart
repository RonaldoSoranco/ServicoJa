import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../../core/state/lista_paginada_controller.dart';
import '../../../core/widgets/lista_paginada_view.dart';
import '../../auth/models/usuario.dart';
import '../data/admin_repository.dart';
import '../models/usuario_admin.dart';
import 'botao_sair_admin.dart';

/// Lista de usuarios do admin — somente leitura, a API nao expoe endpoint
/// de acao (ativar/desativar), entao nao ha botoes aqui.
class AdminUsuariosScreen extends StatefulWidget {
  const AdminUsuariosScreen({super.key});

  @override
  State<AdminUsuariosScreen> createState() => _AdminUsuariosScreenState();
}

class _AdminUsuariosScreenState extends State<AdminUsuariosScreen> {
  final _repositorio = AdminRepository();
  final _formatoData = DateFormat('dd/MM/yyyy', 'pt_BR');
  late final ListaPaginadaController<UsuarioAdmin> _controller;

  @override
  void initState() {
    super.initState();
    _controller = ListaPaginadaController<UsuarioAdmin>(
      buscar: (pagina, tamanho) => _repositorio.listarUsuarios(pagina: pagina, tamanho: tamanho),
    )..carregarInicial();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  String _rotuloPerfil(Perfil perfil) {
    switch (perfil) {
      case Perfil.cliente:
        return 'Cliente';
      case Perfil.empresa:
        return 'Empresa';
      case Perfil.admin:
        return 'Admin';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Usuarios'), actions: const [BotaoSairAdmin()]),
      body: ListaPaginadaView<UsuarioAdmin>(
        controller: _controller,
        mensagemVazia: 'Nenhum usuario encontrado.',
        iconeVazio: Icons.people_outline,
        construirItem: (context, usuario, indice) => Card(
          margin: const EdgeInsets.only(bottom: 10),
          elevation: 0,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12), side: BorderSide(color: Colors.grey.shade200)),
          child: Padding(
            padding: const EdgeInsets.all(14),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(usuario.nome, style: const TextStyle(fontWeight: FontWeight.bold)),
                      const SizedBox(height: 2),
                      Text(usuario.email, style: const TextStyle(color: Colors.black54, fontSize: 13)),
                      const SizedBox(height: 2),
                      Text('Desde ${_formatoData.format(usuario.criadoEm)}', style: const TextStyle(color: Colors.black45, fontSize: 12)),
                    ],
                  ),
                ),
                const SizedBox(width: 12),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Chip(label: Text(_rotuloPerfil(usuario.perfil)), visualDensity: VisualDensity.compact),
                    const SizedBox(height: 4),
                    Text(
                      usuario.ativo ? 'Ativo' : 'Inativo',
                      style: TextStyle(fontSize: 11, color: usuario.ativo ? Colors.green : Colors.red),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
