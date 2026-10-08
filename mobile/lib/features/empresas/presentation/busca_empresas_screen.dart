import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:provider/provider.dart';

import '../../../core/localizacao/localizacao_service.dart';
import '../../../core/network/pagina_resposta.dart';
import '../../../core/state/lista_paginada_controller.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/lista_paginada_view.dart';
import '../../../core/widgets/mapa.dart';
import '../../auth/state/auth_controller.dart';
import '../../categorias/data/categoria_repository.dart';
import '../../categorias/models/categoria.dart';
import '../../categorias/presentation/visual_categoria.dart';
import '../data/empresa_repository.dart';
import '../models/empresa_simples.dart';
import 'empresa_detalhe_screen.dart';
import 'widgets/cartao_empresa.dart';

class BuscaEmpresasScreen extends StatefulWidget {
  const BuscaEmpresasScreen({super.key});

  @override
  State<BuscaEmpresasScreen> createState() => _BuscaEmpresasScreenState();
}

class _BuscaEmpresasScreenState extends State<BuscaEmpresasScreen> {
  final _repositorio = EmpresaRepository();
  final _localizacao = LocalizacaoService();
  final _nomeController = TextEditingController();

  int? _categoriaId;
  bool _somenteAbertas = false;
  LatLng? _posicao;
  bool _obtendoPosicao = false;
  bool _modoMapa = false;

  late final ListaPaginadaController<EmpresaSimples> _controller;
  late final Future<List<Categoria>> _categoriasFuture;

  @override
  void initState() {
    super.initState();
    _categoriasFuture = CategoriaRepository().listarAtivas();
    _controller = ListaPaginadaController<EmpresaSimples>(buscar: _buscarPagina, tamanhoPagina: 30)..carregarInicial();
  }

  @override
  void dispose() {
    _nomeController.dispose();
    _controller.dispose();
    super.dispose();
  }

  Future<PaginaResposta<EmpresaSimples>> _buscarPagina(int pagina, int tamanho) {
    return _repositorio.buscar(
      categoriaId: _categoriaId,
      nome: _nomeController.text,
      latitude: _posicao?.latitude,
      longitude: _posicao?.longitude,
      somenteAbertas: _somenteAbertas,
      pagina: pagina,
      tamanho: tamanho,
    );
  }

  bool get _temFiltrosAtivos =>
      _categoriaId != null || _somenteAbertas || _nomeController.text.trim().isNotEmpty;

  void _recarregar() => _controller.reiniciar(_buscarPagina);

  void _selecionarCategoria(int? id) {
    setState(() => _categoriaId = id);
    _recarregar();
  }

  void _alternarAbertas() {
    setState(() => _somenteAbertas = !_somenteAbertas);
    _recarregar();
  }

  Future<void> _alternarPertoDeMim() async {
    if (_posicao != null) {
      setState(() => _posicao = null);
      _recarregar();
      return;
    }
    setState(() => _obtendoPosicao = true);
    final posicao = await _localizacao.posicaoAtual();
    if (!mounted) return;
    setState(() {
      _obtendoPosicao = false;
      _posicao = posicao;
    });
    if (posicao == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text('Não conseguimos sua localização. Verifique se a permissão de localização está liberada.'),
      ));
      return;
    }
    _recarregar();
  }

  void _limparFiltros() {
    setState(() {
      _categoriaId = null;
      _somenteAbertas = false;
      _nomeController.clear();
    });
    _recarregar();
  }

  void _abrirEmpresa(EmpresaSimples empresa) {
    Navigator.of(context).push(MaterialPageRoute(builder: (_) => EmpresaDetalheScreen(empresaId: empresa.id)));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            _cabecalho(),
            _filtrosRapidos(),
            Expanded(
              child: AnimatedSwitcher(
                duration: const Duration(milliseconds: 250),
                child: _modoMapa
                    ? _MapaResultados(
                        key: const ValueKey('mapa'),
                        controller: _controller,
                        posicao: _posicao,
                        aoAbrir: _abrirEmpresa,
                      )
                    : ListaPaginadaView<EmpresaSimples>(
                        key: const ValueKey('lista'),
                        controller: _controller,
                        padding: const EdgeInsets.fromLTRB(16, 4, 16, 24),
                        mensagemVazia: _temFiltrosAtivos
                            ? 'Nenhuma empresa encontrada com esses filtros.'
                            : 'Nenhuma empresa por aqui ainda.',
                        iconeVazio: Icons.search_off_rounded,
                        acaoVazia: _temFiltrosAtivos ? _limparFiltros : null,
                        rotuloAcaoVazia: 'Limpar filtros',
                        topoDaLista: _temFiltrosAtivos ? null : _Destaques(controller: _controller, aoAbrir: _abrirEmpresa),
                        construirItem: (context, empresa, _) =>
                            CartaoEmpresa(empresa: empresa, aoTocar: () => _abrirEmpresa(empresa)),
                      ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _cabecalho() {
    final nome = context.watch<AuthController>().usuario?.nome.split(' ').first ?? '';
    final texto = Theme.of(context).textTheme;
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(nome.isEmpty ? 'Olá!' : 'Olá, $nome!',
                        style: texto.bodyMedium?.copyWith(color: AppCores.textoSecundario, fontWeight: FontWeight.w600)),
                    const SizedBox(height: 2),
                    Text('Do que você precisa hoje?',
                        style: texto.titleLarge?.copyWith(fontWeight: FontWeight.w800, letterSpacing: -0.3)),
                  ],
                ),
              ),
              IconButton.filledTonal(
                tooltip: _modoMapa ? 'Ver em lista' : 'Ver no mapa',
                style: IconButton.styleFrom(backgroundColor: AppCores.laranjaClaro, foregroundColor: AppCores.laranjaEscuro),
                onPressed: () => setState(() => _modoMapa = !_modoMapa),
                icon: Icon(_modoMapa ? Icons.view_list_rounded : Icons.map_rounded),
              ),
            ],
          ),
          const SizedBox(height: 14),
          TextField(
            controller: _nomeController,
            textInputAction: TextInputAction.search,
            onSubmitted: (_) => _recarregar(),
            onChanged: (_) => setState(() {}),
            decoration: InputDecoration(
              hintText: 'Eletricista, salão, mecânico...',
              prefixIcon: const Icon(Icons.search_rounded),
              suffixIcon: _nomeController.text.isEmpty
                  ? null
                  : IconButton(
                      tooltip: 'Limpar',
                      icon: const Icon(Icons.close_rounded),
                      onPressed: () {
                        _nomeController.clear();
                        _recarregar();
                      },
                    ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _filtrosRapidos() {
    return SizedBox(
      height: 48,
      child: FutureBuilder<List<Categoria>>(
        future: _categoriasFuture,
        builder: (context, snapshot) {
          final categorias = snapshot.data ?? const <Categoria>[];
          return ListView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
            children: [
              _chip(
                rotulo: 'Perto de mim',
                icone: Icons.near_me_rounded,
                selecionado: _posicao != null,
                carregando: _obtendoPosicao,
                aoTocar: _obtendoPosicao ? null : _alternarPertoDeMim,
              ),
              _chip(
                rotulo: 'Abertos agora',
                icone: Icons.schedule_rounded,
                selecionado: _somenteAbertas,
                aoTocar: _alternarAbertas,
              ),
              const VerticalDivider(width: 20, indent: 8, endIndent: 8),
              _chip(
                rotulo: 'Todas',
                icone: Icons.apps_rounded,
                selecionado: _categoriaId == null,
                aoTocar: () => _selecionarCategoria(null),
              ),
              ...categorias.map((c) {
                final visual = VisualCategoria.de(c.icone);
                return _chip(
                  rotulo: c.nome,
                  icone: visual.icone,
                  corIcone: visual.cor,
                  selecionado: _categoriaId == c.id,
                  aoTocar: () => _selecionarCategoria(_categoriaId == c.id ? null : c.id),
                );
              }),
            ],
          );
        },
      ),
    );
  }

  Widget _chip({
    required String rotulo,
    required IconData icone,
    required bool selecionado,
    required VoidCallback? aoTocar,
    Color? corIcone,
    bool carregando = false,
  }) {
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: FilterChip(
        selected: selecionado,
        showCheckmark: false,
        onSelected: aoTocar == null ? null : (_) => aoTocar(),
        avatar: carregando
            ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2))
            : Icon(icone, size: 18, color: selecionado ? Colors.white : (corIcone ?? AppCores.laranja)),
        label: Text(rotulo),
        labelStyle: TextStyle(
          color: selecionado ? Colors.white : AppCores.marinho,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

/// Carrossel com as empresas em destaque (Premium) que vieram na busca.
class _Destaques extends StatelessWidget {
  const _Destaques({required this.controller, required this.aoAbrir});

  final ListaPaginadaController<EmpresaSimples> controller;
  final ValueChanged<EmpresaSimples> aoAbrir;

  @override
  Widget build(BuildContext context) {
    final destaques = controller.itens.where((e) => e.destaque).toList();
    if (destaques.isEmpty) return const SizedBox(height: 4);
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Padding(
            padding: EdgeInsets.only(top: 6, bottom: 10),
            child: Text('Em destaque', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 17)),
          ),
          SizedBox(
            height: 130,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              itemCount: destaques.length,
              separatorBuilder: (_, _) => const SizedBox(width: 12),
              itemBuilder: (context, i) =>
                  CartaoDestaque(empresa: destaques[i], aoTocar: () => aoAbrir(destaques[i])),
            ),
          ),
          const Padding(
            padding: EdgeInsets.only(top: 18),
            child: Text('Todas as empresas', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 17)),
          ),
        ],
      ),
    );
  }
}

/// Resultados da busca como pinos no mapa; tocar num pino mostra um resumo da empresa.
class _MapaResultados extends StatefulWidget {
  const _MapaResultados({super.key, required this.controller, required this.posicao, required this.aoAbrir});

  final ListaPaginadaController<EmpresaSimples> controller;
  final LatLng? posicao;
  final ValueChanged<EmpresaSimples> aoAbrir;

  @override
  State<_MapaResultados> createState() => _MapaResultadosState();
}

class _MapaResultadosState extends State<_MapaResultados> {
  EmpresaSimples? _selecionada;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: widget.controller,
      builder: (context, _) {
        final comLocalizacao = widget.controller.itens.where((e) => e.temLocalizacao).toList();
        final centro = widget.posicao ??
            (comLocalizacao.isNotEmpty ? LatLng(comLocalizacao.first.latitude!, comLocalizacao.first.longitude!) : centroPadrao);
        return Stack(
          children: [
            FlutterMap(
              options: MapOptions(
                initialCenter: centro,
                initialZoom: 14,
                interactionOptions: const InteractionOptions(flags: InteractiveFlag.all & ~InteractiveFlag.rotate),
                onTap: (_, _) => setState(() => _selecionada = null),
              ),
              children: [
                camadaMapa(),
                MarkerLayer(
                  markers: [
                    if (widget.posicao != null)
                      Marker(point: widget.posicao!, width: 22, height: 22, child: const MarcadorUsuario()),
                    ...comLocalizacao.map((empresa) => marcadorEmpresa(
                          LatLng(empresa.latitude!, empresa.longitude!),
                          selecionado: _selecionada?.id == empresa.id,
                          aoTocar: () => setState(() => _selecionada = empresa),
                        )),
                  ],
                ),
                creditoMapa(),
              ],
            ),
            if (widget.controller.carregando) const LinearProgressIndicator(minHeight: 3),
            if (!widget.controller.carregando && comLocalizacao.isEmpty)
              const Positioned(
                top: 12,
                left: 16,
                right: 16,
                child: Card(
                  child: Padding(
                    padding: EdgeInsets.all(14),
                    child: Text('Nenhuma empresa desta busca marcou a localização no mapa ainda.'),
                  ),
                ),
              ),
            if (_selecionada != null)
              Positioned(
                left: 16,
                right: 16,
                bottom: 20,
                child: CartaoEmpresa(empresa: _selecionada!, aoTocar: () => widget.aoAbrir(_selecionada!)),
              ),
          ],
        );
      },
    );
  }
}
