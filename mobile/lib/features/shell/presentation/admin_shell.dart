import 'package:flutter/material.dart';

import '../../../core/widgets/app_shell_scaffold.dart';
import '../../admin/presentation/admin_avaliacoes_screen.dart';
import '../../admin/presentation/admin_dashboard_screen.dart';
import '../../admin/presentation/admin_empresas_screen.dart';
import '../../admin/presentation/admin_logs_screen.dart';
import '../../admin/presentation/admin_usuarios_screen.dart';

/// Shell de navegacao para usuarios ADMIN.
class AdminShell extends StatelessWidget {
  const AdminShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const AppShellScaffold(
      itens: [
        ItemNavegacao(rotulo: 'Painel', icone: Icon(Icons.dashboard_outlined), iconeSelecionado: Icon(Icons.dashboard), tela: AdminDashboardScreen()),
        ItemNavegacao(rotulo: 'Empresas', icone: Icon(Icons.storefront_outlined), iconeSelecionado: Icon(Icons.storefront), tela: AdminEmpresasScreen()),
        ItemNavegacao(rotulo: 'Avaliacoes', icone: Icon(Icons.rate_review_outlined), iconeSelecionado: Icon(Icons.rate_review), tela: AdminAvaliacoesScreen()),
        ItemNavegacao(rotulo: 'Usuarios', icone: Icon(Icons.people_outline), iconeSelecionado: Icon(Icons.people), tela: AdminUsuariosScreen()),
        ItemNavegacao(rotulo: 'Logs', icone: Icon(Icons.receipt_long_outlined), iconeSelecionado: Icon(Icons.receipt_long), tela: AdminLogsScreen()),
      ],
    );
  }
}
