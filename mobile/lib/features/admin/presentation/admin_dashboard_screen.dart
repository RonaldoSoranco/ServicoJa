import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/estado_erro.dart';
import '../data/admin_repository.dart';
import '../models/estatisticas.dart';
import 'botao_sair_admin.dart';

class AdminDashboardScreen extends StatefulWidget {
  const AdminDashboardScreen({super.key});

  @override
  State<AdminDashboardScreen> createState() => _AdminDashboardScreenState();
}

class _AdminDashboardScreenState extends State<AdminDashboardScreen> {
  final _repositorio = AdminRepository();
  bool _carregando = true;
  String? _erro;
  Estatisticas? _estatisticas;

  @override
  void initState() {
    super.initState();
    _carregar();
  }

  Future<void> _carregar() async {
    setState(() {
      _carregando = true;
      _erro = null;
    });
    try {
      final estatisticas = await _repositorio.estatisticas();
      if (mounted) setState(() => _estatisticas = estatisticas);
    } on ApiException catch (e) {
      if (mounted) setState(() => _erro = e.toString());
    } catch (_) {
      if (mounted) setState(() => _erro = 'Ocorreu um erro inesperado. Tente novamente.');
    } finally {
      if (mounted) setState(() => _carregando = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Painel administrativo'), actions: const [BotaoSairAdmin()]),
      body: _carregando
          ? const Center(child: CircularProgressIndicator())
          : _erro != null
              ? EstadoErro(mensagem: _erro!, aoTentarNovamente: _carregar)
              : RefreshIndicator(
                  onRefresh: _carregar,
                  child: GridView.count(
                    padding: const EdgeInsets.all(16),
                    crossAxisCount: 2,
                    mainAxisSpacing: 12,
                    crossAxisSpacing: 12,
                    childAspectRatio: 1.4,
                    children: [
                      _cartao('Usuarios', _estatisticas!.totalUsuarios, Icons.people_outline, Colors.blue),
                      _cartao('Clientes', _estatisticas!.totalClientes, Icons.person_outline, Colors.indigo),
                      _cartao('Empresas', _estatisticas!.totalEmpresas, Icons.storefront_outlined, Colors.teal),
                      _cartao('Empresas Premium', _estatisticas!.totalEmpresasPremium, Icons.workspace_premium_outlined, Colors.amber.shade800),
                      _cartao('Empresas pendentes', _estatisticas!.empresasPendentes, Icons.hourglass_top, Colors.orange),
                      _cartao('Avaliacoes pendentes', _estatisticas!.avaliacoesPendentes, Icons.rate_review_outlined, Colors.deepOrange),
                      _cartao('Avaliacoes aprovadas', _estatisticas!.avaliacoesAprovadas, Icons.check_circle_outline, Colors.green),
                      _cartao('Total de avaliacoes', _estatisticas!.totalAvaliacoes, Icons.star_outline, Colors.purple),
                    ],
                  ),
                ),
    );
  }

  Widget _cartao(String titulo, int valor, IconData icone, Color cor) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: cor.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: cor.withValues(alpha: 0.2)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Icon(icone, color: cor),
          Text('$valor', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: cor)),
          Text(titulo, style: const TextStyle(fontSize: 12, color: Colors.black54)),
        ],
      ),
    );
  }
}
