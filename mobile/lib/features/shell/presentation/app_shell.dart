import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/widgets/app_shell_scaffold.dart';
import '../../auth/models/usuario.dart';
import '../../auth/state/auth_controller.dart';
import '../../empresas/presentation/busca_empresas_screen.dart';
import '../../empresas/presentation/minhas_empresas_screen.dart';
import '../../favoritos/presentation/favoritos_screen.dart';
import '../../notificacoes/presentation/notificacoes_screen.dart';
import '../../notificacoes/state/notificacao_badge_controller.dart';
import '../../perfil/presentation/perfil_screen.dart';

/// Shell de navegacao para usuarios CLIENTE/EMPRESA.
///
/// As abas variam por perfil: EMPRESA tem "Minha empresa" no lugar de "Favoritos".
class AppShell extends StatefulWidget {
  const AppShell({super.key});

  @override
  State<AppShell> createState() => _AppShellState();
}

class _AppShellState extends State<AppShell> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) context.read<NotificacaoBadgeController>().atualizar();
    });
  }

  @override
  Widget build(BuildContext context) {
    final usuario = context.watch<AuthController>().usuario;
    final ehEmpresa = usuario?.perfil == Perfil.empresa;

    return AppShellScaffold(
      itens: [
        const ItemNavegacao(
          rotulo: 'Buscar',
          icone: Icon(Icons.search_outlined),
          iconeSelecionado: Icon(Icons.search),
          tela: BuscaEmpresasScreen(),
        ),
        if (ehEmpresa)
          const ItemNavegacao(
            rotulo: 'Minha empresa',
            icone: Icon(Icons.storefront_outlined),
            iconeSelecionado: Icon(Icons.storefront),
            tela: MinhasEmpresasScreen(),
          )
        else
          const ItemNavegacao(
            rotulo: 'Favoritos',
            icone: Icon(Icons.favorite_border),
            iconeSelecionado: Icon(Icons.favorite),
            tela: FavoritosScreen(),
          ),
        ItemNavegacao(
          rotulo: 'Notificacoes',
          icone: Consumer<NotificacaoBadgeController>(
            builder: (context, badge, _) {
              final icone = const Icon(Icons.notifications_outlined);
              if (badge.quantidade == 0) return icone;
              return Badge(label: Text('${badge.quantidade}'), child: icone);
            },
          ),
          iconeSelecionado: const Icon(Icons.notifications),
          tela: const NotificacoesScreen(),
        ),
        const ItemNavegacao(
          rotulo: 'Perfil',
          icone: Icon(Icons.person_outline),
          iconeSelecionado: Icon(Icons.person),
          tela: PerfilScreen(),
        ),
      ],
    );
  }
}
