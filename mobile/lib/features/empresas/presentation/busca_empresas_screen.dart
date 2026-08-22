import 'package:flutter/material.dart';

import '../../../core/network/pagina_resposta.dart';
import '../../../core/state/lista_paginada_controller.dart';
import '../../../core/widgets/estrelas.dart';
import '../../../core/widgets/lista_paginada_view.dart';
import '../../categorias/data/categoria_repository.dart';
import '../../categorias/models/categoria.dart';
import '../data/empresa_repository.dart';
import '../models/empresa_simples.dart';
import 'empresa_detalhe_screen.dart';

class BuscaEmpresasScreen extends StatefulWidget {
  const BuscaEmpresasScreen({super.key});

  @override
  State<BuscaEmpresasScreen> createState() => _BuscaEmpresasScreenState();
}

class _BuscaEmpresasScreenState extends State<BuscaEmpresasScreen> {
  final _repositorio = EmpresaRepository();
  final _nomeController = TextEditingController();
  final _cidadeController = TextEditingController();
  final _ufController = TextEditingController();
  int? _categoriaId;
  bool _filtrosAbertos = false;

  late final ListaPaginadaController<EmpresaSimples> _controller;
  late final Future<List<Categoria>> _categoriasFuture;

  @override
  void initState() {
    super.initState();
    _categoriasFuture = CategoriaRepository().listarAtivas();
    _controller = ListaPaginadaController<EmpresaSimples>(buscar: _buscarPagina)..carregarInicial();
  }

  @override
  void dispose() {
    _nomeController.dispose();
    _cidadeController.dispose();
    _ufController.dispose();
    _controller.dispose();
    super.dispose();
  }

  Future<PaginaResposta<EmpresaSimples>> _buscarPagina(int pagina, int tamanho) {
    return _repositorio.buscar(
      categoriaId: _categoriaId,
      nome: _nomeController.text,
      cidade: _cidadeController.text,
      uf: _ufController.text,
      pagina: pagina,
      tamanho: tamanho,
    );
  }

  bool get _temFiltrosAtivos =>
      _categoriaId != null ||
      _nomeController.text.trim().isNotEmpty ||
      _cidadeController.text.trim().isNotEmpty ||
      _ufController.text.trim().isNotEmpty;

  void _aplicarFiltros() => _controller.reiniciar(_buscarPagina);

  void _limparFiltros() {
    setState(() {
      _categoriaId = null;
      _nomeController.clear();
      _cidadeController.clear();
      _ufController.clear();
    });
    _aplicarFiltros();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Buscar empresas')),
      body: ListaPaginadaView<EmpresaSimples>(
        controller: _controller,
        mensagemVazia: _temFiltrosAtivos
            ? 'Nenhuma empresa encontrada com esses filtros.'
            : 'Nenhuma empresa aprovada ainda.',
        iconeVazio: Icons.search_off,
        acaoVazia: _temFiltrosAtivos ? _limparFiltros : null,
        rotuloAcaoVazia: 'Limpar filtros',
        cabecalho: _construirFiltros(),
        construirItem: (context, empresa, indice) => _CartaoEmpresa(
          empresa: empresa,
          onTap: () => Navigator.of(context).push(
            MaterialPageRoute(builder: (_) => EmpresaDetalheScreen(empresaId: empresa.id)),
          ),
        ),
      ),
    );
  }

  Widget _construirFiltros() {
    return Container(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 4),
      color: Theme.of(context).scaffoldBackgroundColor,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _nomeController,
                  textInputAction: TextInputAction.search,
                  decoration: const InputDecoration(
                    hintText: 'Buscar por nome...',
                    prefixIcon: Icon(Icons.search),
                    isDense: true,
                  ),
                  onSubmitted: (_) => _aplicarFiltros(),
                ),
              ),
              IconButton(
                tooltip: _filtrosAbertos ? 'Ocultar filtros' : 'Mais filtros',
                icon: Icon(_filtrosAbertos ? Icons.filter_alt : Icons.filter_alt_outlined),
                onPressed: () => setState(() => _filtrosAbertos = !_filtrosAbertos),
              ),
            ],
          ),
          if (_filtrosAbertos) ...[
            const SizedBox(height: 8),
            FutureBuilder<List<Categoria>>(
              future: _categoriasFuture,
              builder: (context, snapshot) {
                final categorias = snapshot.data ?? const <Categoria>[];
                return DropdownButtonFormField<int?>(
                  initialValue: _categoriaId,
                  decoration: const InputDecoration(labelText: 'Categoria', isDense: true),
                  items: [
                    const DropdownMenuItem<int?>(value: null, child: Text('Todas as categorias')),
                    ...categorias.map((c) => DropdownMenuItem<int?>(value: c.id, child: Text(c.nome))),
                  ],
                  onChanged: (v) {
                    setState(() => _categoriaId = v);
                    _aplicarFiltros();
                  },
                );
              },
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  flex: 2,
                  child: TextField(
                    controller: _cidadeController,
                    decoration: const InputDecoration(labelText: 'Cidade', isDense: true),
                    onSubmitted: (_) => _aplicarFiltros(),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: TextField(
                    controller: _ufController,
                    maxLength: 2,
                    textCapitalization: TextCapitalization.characters,
                    decoration: const InputDecoration(labelText: 'UF', isDense: true, counterText: ''),
                    onSubmitted: (_) => _aplicarFiltros(),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Align(
              alignment: Alignment.centerRight,
              child: Wrap(
                spacing: 8,
                children: [
                  if (_temFiltrosAtivos) TextButton(onPressed: _limparFiltros, child: const Text('Limpar')),
                  FilledButton(onPressed: _aplicarFiltros, child: const Text('Aplicar filtros')),
                ],
              ),
            ),
          ],
          const Divider(height: 16),
        ],
      ),
    );
  }
}

class _CartaoEmpresa extends StatelessWidget {
  const _CartaoEmpresa({required this.empresa, required this.onTap});

  final EmpresaSimples empresa;
  final VoidCallback onTap;

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
              _Logo(url: empresa.logoUrl),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            empresa.nome,
                            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                        if (empresa.destaque)
                          Container(
                            margin: const EdgeInsets.only(left: 6),
                            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                            decoration: BoxDecoration(
                              color: Theme.of(context).colorScheme.secondary.withValues(alpha: 0.15),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Text(
                              'Destaque',
                              style: TextStyle(
                                fontSize: 10,
                                fontWeight: FontWeight.bold,
                                color: Theme.of(context).colorScheme.secondary,
                              ),
                            ),
                          ),
                      ],
                    ),
                    const SizedBox(height: 2),
                    Text(empresa.categoria.nome, style: const TextStyle(color: Colors.black54, fontSize: 13)),
                    const SizedBox(height: 4),
                    Text(
                      '${empresa.cidade} - ${empresa.uf}',
                      style: const TextStyle(color: Colors.black45, fontSize: 12),
                    ),
                    const SizedBox(height: 4),
                    if (empresa.mediaAvaliacoes != null && (empresa.totalAvaliacoes ?? 0) > 0)
                      Row(
                        children: [
                          Estrelas(nota: empresa.mediaAvaliacoes!, tamanho: 14),
                          const SizedBox(width: 4),
                          Text(
                            '${empresa.mediaAvaliacoes!.toStringAsFixed(1)} (${empresa.totalAvaliacoes})',
                            style: const TextStyle(fontSize: 12, color: Colors.black54),
                          ),
                        ],
                      )
                    else
                      const Text('Sem avaliacoes ainda', style: TextStyle(fontSize: 12, color: Colors.black38)),
                  ],
                ),
              ),
              const Icon(Icons.chevron_right, color: Colors.black38),
            ],
          ),
        ),
      ),
    );
  }
}

class _Logo extends StatelessWidget {
  const _Logo({required this.url});

  final String? url;

  @override
  Widget build(BuildContext context) {
    final borda = BorderRadius.circular(12);
    if (url == null || url!.isEmpty) {
      return Container(
        width: 56,
        height: 56,
        decoration: BoxDecoration(color: Colors.grey.shade100, borderRadius: borda),
        child: const Icon(Icons.storefront_outlined, color: Colors.black26),
      );
    }
    return ClipRRect(
      borderRadius: borda,
      child: Image.network(
        url!,
        width: 56,
        height: 56,
        fit: BoxFit.cover,
        errorBuilder: (context, error, stack) => Container(
          width: 56,
          height: 56,
          color: Colors.grey.shade100,
          child: const Icon(Icons.broken_image_outlined, color: Colors.black26),
        ),
      ),
    );
  }
}
