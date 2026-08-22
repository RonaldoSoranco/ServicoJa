import 'package:flutter/material.dart';

/// Uma aba do shell de navegacao (bottom nav).
class ItemNavegacao {
  const ItemNavegacao({required this.rotulo, required this.icone, required this.tela, this.iconeSelecionado});

  final String rotulo;
  final Widget icone;
  final Widget? iconeSelecionado;
  final Widget tela;
}

/// IndexedStack + NavigationBar generico, reusado pelo shell do cliente/empresa e pelo admin.
class AppShellScaffold extends StatefulWidget {
  const AppShellScaffold({super.key, required this.itens, this.indiceInicial = 0});

  final List<ItemNavegacao> itens;
  final int indiceInicial;

  @override
  State<AppShellScaffold> createState() => _AppShellScaffoldState();
}

class _AppShellScaffoldState extends State<AppShellScaffold> {
  late int _indice = widget.indiceInicial;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      // Reconstroi a aba selecionada a cada troca (em vez de IndexedStack) para que
      // telas com dados carregados no initState (Favoritos, Notificacoes, Minha
      // Empresa) sempre busquem dados atualizados ao serem reabertas.
      body: KeyedSubtree(
        key: ValueKey(_indice),
        child: widget.itens[_indice].tela,
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _indice,
        onDestinationSelected: (indice) => setState(() => _indice = indice),
        destinations: widget.itens
            .map((item) => NavigationDestination(
                  icon: item.icone,
                  selectedIcon: item.iconeSelecionado,
                  label: item.rotulo,
                ))
            .toList(),
      ),
    );
  }
}
