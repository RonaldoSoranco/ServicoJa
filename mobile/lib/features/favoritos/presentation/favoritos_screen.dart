import 'package:flutter/material.dart';

import '../../../core/state/lista_paginada_controller.dart';
import '../../../core/widgets/estrelas.dart';
import '../../../core/widgets/lista_paginada_view.dart';
import '../../empresas/presentation/empresa_detalhe_screen.dart';
import '../data/favorito_repository.dart';
import '../models/favorito.dart';

class FavoritosScreen extends StatefulWidget {
  const FavoritosScreen({super.key});

  @override
  State<FavoritosScreen> createState() => _FavoritosScreenState();
}

class _FavoritosScreenState extends State<FavoritosScreen> {
  final _repositorio = FavoritoRepository();
  late final ListaPaginadaController<Favorito> _controller;

  @override
  void initState() {
    super.initState();
    _controller = ListaPaginadaController<Favorito>(
      buscar: (pagina, tamanho) => _repositorio.listar(pagina: pagina, tamanho: tamanho),
    )..carregarInicial();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _remover(Favorito favorito) async {
    _controller.removerLocal((f) => f.empresaId == favorito.empresaId);
    try {
      await _repositorio.remover(favorito.empresaId);
    } catch (_) {
      _controller.inserirLocal(0, favorito);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Nao foi possivel remover dos favoritos. Tente novamente.')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Favoritos')),
      body: ListaPaginadaView<Favorito>(
        controller: _controller,
        mensagemVazia: 'Voce ainda nao favoritou nenhuma empresa.\nToque no coracao no perfil de uma empresa para salva-la aqui.',
        iconeVazio: Icons.favorite_border,
        construirItem: (context, favorito, indice) => _CartaoFavorito(
          favorito: favorito,
          onTap: () => Navigator.of(context).push(
            MaterialPageRoute(builder: (_) => EmpresaDetalheScreen(empresaId: favorito.empresaId)),
          ),
          onRemover: () => _remover(favorito),
        ),
      ),
    );
  }
}

class _CartaoFavorito extends StatelessWidget {
  const _CartaoFavorito({required this.favorito, required this.onTap, required this.onRemover});

  final Favorito favorito;
  final VoidCallback onTap;
  final VoidCallback onRemover;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16), side: BorderSide(color: Colors.grey.shade200)),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Row(
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: favorito.logoUrl != null && favorito.logoUrl!.isNotEmpty
                    ? Image.network(
                        favorito.logoUrl!,
                        width: 56,
                        height: 56,
                        fit: BoxFit.cover,
                        errorBuilder: (_, __, ___) => _placeholder(),
                      )
                    : _placeholder(),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(favorito.nomeEmpresa, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                    const SizedBox(height: 4),
                    Text('${favorito.cidade} - ${favorito.uf}', style: const TextStyle(color: Colors.black45, fontSize: 12)),
                    const SizedBox(height: 4),
                    if (favorito.mediaAvaliacoes != null && (favorito.totalAvaliacoes ?? 0) > 0)
                      Row(
                        children: [
                          Estrelas(nota: favorito.mediaAvaliacoes!, tamanho: 14),
                          const SizedBox(width: 4),
                          Text(favorito.mediaAvaliacoes!.toStringAsFixed(1), style: const TextStyle(fontSize: 12)),
                        ],
                      ),
                  ],
                ),
              ),
              IconButton(
                tooltip: 'Remover dos favoritos',
                icon: const Icon(Icons.favorite, color: Colors.red),
                onPressed: onRemover,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _placeholder() {
    return Container(
      width: 56,
      height: 56,
      color: Colors.grey.shade100,
      child: const Icon(Icons.storefront_outlined, color: Colors.black26),
    );
  }
}
