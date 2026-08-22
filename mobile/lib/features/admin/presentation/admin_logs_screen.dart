import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../../core/state/lista_paginada_controller.dart';
import '../../../core/widgets/lista_paginada_view.dart';
import '../data/admin_repository.dart';
import '../models/log_admin.dart';
import 'botao_sair_admin.dart';

class AdminLogsScreen extends StatefulWidget {
  const AdminLogsScreen({super.key});

  @override
  State<AdminLogsScreen> createState() => _AdminLogsScreenState();
}

class _AdminLogsScreenState extends State<AdminLogsScreen> {
  final _repositorio = AdminRepository();
  final _formatoData = DateFormat('dd/MM/yyyy HH:mm', 'pt_BR');
  late final ListaPaginadaController<LogAdmin> _controller;

  @override
  void initState() {
    super.initState();
    _controller = ListaPaginadaController<LogAdmin>(
      buscar: (pagina, tamanho) => _repositorio.listarLogs(pagina: pagina, tamanho: tamanho),
      tamanhoPagina: 20,
    )..carregarInicial();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Logs'), actions: const [BotaoSairAdmin()]),
      body: ListaPaginadaView<LogAdmin>(
        controller: _controller,
        mensagemVazia: 'Nenhum log registrado.',
        iconeVazio: Icons.receipt_long_outlined,
        construirItem: (context, log, indice) => Card(
          margin: const EdgeInsets.only(bottom: 10),
          elevation: 0,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12), side: BorderSide(color: Colors.grey.shade200)),
          child: ListTile(
            title: Text(log.acao, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (log.detalhes != null && log.detalhes!.isNotEmpty) Text(log.detalhes!),
                const SizedBox(height: 4),
                Text(
                  '${log.usuario} - ${_formatoData.format(log.criadoEm)}${log.ip != null ? ' - ${log.ip}' : ''}',
                  style: const TextStyle(fontSize: 11, color: Colors.black45),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
