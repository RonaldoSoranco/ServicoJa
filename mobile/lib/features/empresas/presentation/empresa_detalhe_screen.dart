import 'dart:async';

import 'package:flutter/material.dart';
import 'package:latlong2/latlong.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/estado_erro.dart';
import '../../../core/widgets/estrelas.dart';
import '../../../core/widgets/mapa.dart';
import '../../../core/widgets/selo.dart';
import '../../auth/models/usuario.dart';
import '../../auth/state/auth_controller.dart';
import '../../avaliacoes/data/avaliacao_repository.dart';
import '../../avaliacoes/models/avaliacao.dart';
import '../../avaliacoes/presentation/avaliar_dialog.dart';
import '../../categorias/presentation/visual_categoria.dart';
import '../../favoritos/data/favorito_repository.dart';
import '../data/contato_empresa.dart';
import '../data/empresa_repository.dart';
import '../models/desempenho.dart';
import '../models/empresa.dart';
import '../models/horario.dart';
import 'widgets/cartao_empresa.dart';

class EmpresaDetalheScreen extends StatefulWidget {
  const EmpresaDetalheScreen({super.key, required this.empresaId});

  final int empresaId;

  @override
  State<EmpresaDetalheScreen> createState() => _EmpresaDetalheScreenState();
}

class _EmpresaDetalheScreenState extends State<EmpresaDetalheScreen> {
  final _empresaRepositorio = EmpresaRepository();
  final _favoritoRepositorio = FavoritoRepository();
  final _avaliacaoRepositorio = AvaliacaoRepository();

  Empresa? _empresa;
  bool _carregando = true;
  String? _erro;

  bool _favoritado = false;
  bool _favoritoCarregando = false;

  List<Avaliacao> _avaliacoes = [];
  bool _avaliacoesCarregando = true;

  @override
  void initState() {
    super.initState();
    _carregar(registrarVisita: true);
  }

  Future<void> _carregar({bool registrarVisita = false}) async {
    setState(() {
      _carregando = _empresa == null;
      _erro = null;
    });
    try {
      final empresa = await _empresaRepositorio.detalhar(widget.empresaId);
      _empresa = empresa;
      if (registrarVisita) unawaited(_empresaRepositorio.registrarEvento(empresa.id, TipoEvento.visualizacao));
      unawaited(_carregarFavorito());
      unawaited(_carregarAvaliacoes());
    } on ApiException catch (e) {
      _erro = e.toString();
    } catch (_) {
      _erro = 'Ocorreu um erro inesperado. Tente novamente.';
    } finally {
      if (mounted) setState(() => _carregando = false);
    }
  }

  Future<void> _carregarFavorito() async {
    final auth = context.read<AuthController>();
    if (auth.usuario?.perfil != Perfil.cliente) return;
    try {
      final favoritado = await _favoritoRepositorio.estaFavoritada(widget.empresaId);
      if (mounted) setState(() => _favoritado = favoritado);
    } catch (_) {
      // Silencioso: o coracao so nao aparece marcado, sem bloquear a tela.
    }
  }

  Future<void> _carregarAvaliacoes() async {
    setState(() => _avaliacoesCarregando = true);
    try {
      final pagina = await _avaliacaoRepositorio.listarPorEmpresa(widget.empresaId, pagina: 0, tamanho: 20);
      if (mounted) setState(() => _avaliacoes = pagina.conteudo);
    } catch (_) {
      // Mantem a secao de avaliacoes vazia silenciosamente em caso de erro pontual.
    } finally {
      if (mounted) setState(() => _avaliacoesCarregando = false);
    }
  }

  Future<void> _alternarFavorito() async {
    if (_favoritoCarregando) return;
    final novoEstado = !_favoritado;
    setState(() {
      _favoritado = novoEstado;
      _favoritoCarregando = true;
    });
    try {
      if (novoEstado) {
        await _favoritoRepositorio.favoritar(widget.empresaId);
      } else {
        await _favoritoRepositorio.remover(widget.empresaId);
      }
    } on ApiException catch (e) {
      if (mounted) {
        setState(() => _favoritado = !novoEstado);
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
      }
    } catch (_) {
      if (mounted) setState(() => _favoritado = !novoEstado);
    } finally {
      if (mounted) setState(() => _favoritoCarregando = false);
    }
  }

  Future<void> _avaliar() async {
    final resultado = await mostrarDialogoAvaliar(context, empresaId: widget.empresaId);
    if (resultado != null && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Avaliação enviada! Ela aparece aqui assim que for aprovada.')),
      );
      _carregarAvaliacoes();
    }
  }

  /// Registra a interação para o painel da empresa e abre o link externo.
  Future<void> _abrir(Uri? uri, {TipoEvento? evento}) async {
    if (uri == null) return;
    if (evento != null) unawaited(_empresaRepositorio.registrarEvento(widget.empresaId, evento));
    try {
      final abriu = await launchUrl(uri, mode: LaunchMode.externalApplication);
      if (!abriu) throw Exception();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Não foi possível abrir o link.')));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthController>();
    final ehCliente = auth.usuario?.perfil == Perfil.cliente;
    final empresa = _empresa;

    if (_carregando) return const Scaffold(body: Center(child: CircularProgressIndicator()));
    if (_erro != null || empresa == null) {
      return Scaffold(
        appBar: AppBar(),
        body: EstadoErro(mensagem: _erro ?? 'Empresa não encontrada.', aoTentarNovamente: _carregar),
      );
    }

    return Scaffold(
      body: RefreshIndicator(
        onRefresh: _carregar,
        edgeOffset: 100,
        child: CustomScrollView(
          slivers: [
            _capa(empresa, ehCliente),
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(20, 18, 20, 32),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _identidade(empresa),
                    const SizedBox(height: 18),
                    _BarraDeAcoes(empresa: empresa, aoAbrir: _abrir),
                    if (_texto(empresa.descricaoCurta) || _texto(empresa.descricaoCompleta))
                      _Secao(
                        titulo: 'Sobre',
                        icone: Icons.info_outline_rounded,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            if (_texto(empresa.descricaoCurta))
                              Text(empresa.descricaoCurta!, style: const TextStyle(fontSize: 15, height: 1.45)),
                            if (_texto(empresa.descricaoCompleta)) ...[
                              const SizedBox(height: 8),
                              Text(empresa.descricaoCompleta!,
                                  style: const TextStyle(height: 1.5, color: AppCores.textoSecundario)),
                            ],
                          ],
                        ),
                      ),
                    if (empresa.horarios.isNotEmpty)
                      _Secao(
                        titulo: 'Horário de funcionamento',
                        icone: Icons.schedule_rounded,
                        child: _QuadroHorarios(horarios: empresa.horarios),
                      ),
                    _Secao(
                      titulo: 'Localização',
                      icone: Icons.place_outlined,
                      child: _localizacao(empresa),
                    ),
                    if (_texto(empresa.emailContato) || _texto(empresa.site) || _texto(empresa.redesSociais))
                      _Secao(titulo: 'Contato', icone: Icons.alternate_email_rounded, child: _contato(empresa)),
                    if (empresa.fotos.isNotEmpty)
                      _Secao(titulo: 'Fotos', icone: Icons.photo_library_outlined, child: _fotos(empresa)),
                    if (empresa.portfolios.isNotEmpty)
                      _Secao(titulo: 'Portfólio', icone: Icons.work_outline_rounded, child: _portfolio(empresa)),
                    _Secao(
                      titulo: 'Avaliações',
                      icone: Icons.star_outline_rounded,
                      acao: ehCliente ? TextButton.icon(onPressed: _avaliar, icon: const Icon(Icons.edit_outlined, size: 18), label: const Text('Avaliar')) : null,
                      child: _avaliacoesSecao(empresa),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  bool _texto(String? valor) => valor != null && valor.trim().isNotEmpty;

  Widget _capa(Empresa empresa, bool ehCliente) {
    final visual = VisualCategoria.de(empresa.categoria.icone);
    return SliverAppBar(
      expandedHeight: 190,
      pinned: true,
      stretch: true,
      backgroundColor: AppCores.laranja,
      foregroundColor: Colors.white,
      title: Text(empresa.nome, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w800)),
      actions: [
        if (ehCliente)
          IconButton(
            tooltip: _favoritado ? 'Remover dos favoritos' : 'Favoritar',
            onPressed: _alternarFavorito,
            icon: AnimatedSwitcher(
              duration: const Duration(milliseconds: 250),
              transitionBuilder: (child, animacao) => ScaleTransition(scale: animacao, child: child),
              child: Icon(
                _favoritado ? Icons.favorite_rounded : Icons.favorite_border_rounded,
                key: ValueKey(_favoritado),
                color: Colors.white,
              ),
            ),
          ),
      ],
      flexibleSpace: FlexibleSpaceBar(
        collapseMode: CollapseMode.parallax,
        background: Container(
          decoration: const BoxDecoration(gradient: AppCores.gradienteMarca),
          child: Stack(
            children: [
              Positioned(
                right: -30,
                bottom: -40,
                child: Icon(visual.icone, size: 210, color: Colors.white.withValues(alpha: 0.13)),
              ),
              Positioned(
                left: 20,
                bottom: 20,
                child: Container(
                  padding: const EdgeInsets.all(3),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(26),
                    boxShadow: const [BoxShadow(color: Colors.black26, blurRadius: 12, offset: Offset(0, 4))],
                  ),
                  child: Hero(
                    tag: tagLogoEmpresa(empresa.id),
                    child: LogoEmpresa(url: empresa.logoUrl, chaveCategoria: empresa.categoria.icone, tamanho: 76),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _identidade(Empresa empresa) {
    final visual = VisualCategoria.de(empresa.categoria.icone);
    final situacao = SituacaoFuncionamento.calcular(empresa.horarios, DateTime.now());
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(empresa.nome,
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w800, letterSpacing: -0.4)),
        const SizedBox(height: 6),
        Row(
          children: [
            Icon(visual.icone, size: 16, color: visual.cor),
            const SizedBox(width: 4),
            Text(empresa.categoria.nome,
                style: const TextStyle(color: AppCores.textoSecundario, fontWeight: FontWeight.w600)),
            const Text('  ·  ', style: TextStyle(color: AppCores.textoSecundario)),
            Flexible(
              child: Text('${empresa.cidade} - ${empresa.uf}',
                  overflow: TextOverflow.ellipsis, style: const TextStyle(color: AppCores.textoSecundario)),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: [
            NotaEmpresa(media: empresa.mediaAvaliacoes, total: empresa.totalAvaliacoes, tamanho: 14),
            if (situacao != null)
              SeloFuncionamento(aberto: situacao.aberto, temHorarios: true, detalhe: situacao.detalhe),
            if (empresa.destaque)
              const Selo(texto: 'Destaque', cor: AppCores.laranjaEscuro, icone: Icons.workspace_premium_rounded),
          ],
        ),
      ],
    );
  }

  Widget _localizacao(Empresa empresa) {
    final endereco = [
      if (_texto(empresa.endereco)) empresa.endereco,
      if (_texto(empresa.numero)) empresa.numero,
      if (_texto(empresa.bairro)) empresa.bairro,
    ].whereType<String>().join(', ');
    final cidade = '${empresa.cidade} - ${empresa.uf}';
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (empresa.temLocalizacao) ...[
          MapaPrevia(
            ponto: LatLng(empresa.latitude!, empresa.longitude!),
            altura: 170,
            aoTocar: () => _abrir(ContatoEmpresa.comoChegar(empresa.latitude!, empresa.longitude!),
                evento: TipoEvento.cliqueMapa),
          ),
          const SizedBox(height: 12),
        ],
        Text(endereco.isEmpty ? cidade : '$endereco\n$cidade', style: const TextStyle(height: 1.45)),
        if (empresa.temLocalizacao) ...[
          const SizedBox(height: 10),
          OutlinedButton.icon(
            onPressed: () => _abrir(ContatoEmpresa.comoChegar(empresa.latitude!, empresa.longitude!),
                evento: TipoEvento.cliqueMapa),
            icon: const Icon(Icons.map_outlined),
            label: const Text('Abrir no Maps'),
          ),
        ],
      ],
    );
  }

  Widget _contato(Empresa empresa) {
    return Column(
      children: [
        if (_texto(empresa.emailContato))
          _linhaContato(Icons.email_outlined, empresa.emailContato!,
              () => _abrir(Uri(scheme: 'mailto', path: empresa.emailContato))),
        if (_texto(empresa.site))
          _linhaContato(Icons.language_rounded, empresa.site!, () {
            final site = empresa.site!.trim();
            _abrir(Uri.tryParse(site.startsWith('http') ? site : 'https://$site'));
          }),
        if (_texto(empresa.redesSociais)) _linhaContato(Icons.tag_rounded, empresa.redesSociais!, null),
      ],
    );
  }

  Widget _linhaContato(IconData icone, String texto, VoidCallback? aoTocar) {
    return ListTile(
      contentPadding: EdgeInsets.zero,
      dense: true,
      leading: Icon(icone, color: AppCores.laranja),
      title: Text(texto, style: const TextStyle(fontWeight: FontWeight.w600)),
      trailing: aoTocar == null ? null : const Icon(Icons.open_in_new_rounded, size: 18),
      onTap: aoTocar,
    );
  }

  Widget _fotos(Empresa empresa) {
    return SizedBox(
      height: 120,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        itemCount: empresa.fotos.length,
        separatorBuilder: (_, _) => const SizedBox(width: 10),
        itemBuilder: (context, i) {
          final foto = empresa.fotos[i];
          return GestureDetector(
            onTap: () => _ampliarFoto(foto.url),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(14),
              child: Image.network(
                foto.url,
                width: 120,
                height: 120,
                fit: BoxFit.cover,
                errorBuilder: (_, _, _) => Container(
                  width: 120,
                  height: 120,
                  color: AppCores.creme,
                  child: const Icon(Icons.broken_image_outlined, color: AppCores.textoSecundario),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  void _ampliarFoto(String url) {
    showDialog<void>(
      context: context,
      builder: (context) => Dialog(
        backgroundColor: Colors.black,
        insetPadding: const EdgeInsets.all(12),
        child: Stack(
          children: [
            InteractiveViewer(child: Image.network(url, fit: BoxFit.contain)),
            Positioned(
              top: 4,
              right: 4,
              child: IconButton(
                onPressed: () => Navigator.of(context).pop(),
                icon: const Icon(Icons.close_rounded, color: Colors.white),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _portfolio(Empresa empresa) {
    return Column(
      children: empresa.portfolios
          .map((p) => Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    if (_texto(p.titulo)) Text(p.titulo!, style: const TextStyle(fontWeight: FontWeight.w700)),
                    if (_texto(p.descricao))
                      Text(p.descricao!, style: const TextStyle(color: AppCores.textoSecundario)),
                    if (_texto(p.urlMidia))
                      Padding(
                        padding: const EdgeInsets.only(top: 6),
                        child: ClipRRect(
                          borderRadius: BorderRadius.circular(14),
                          child: Image.network(
                            p.urlMidia!,
                            height: 140,
                            width: double.infinity,
                            fit: BoxFit.cover,
                            errorBuilder: (_, _, _) => const SizedBox.shrink(),
                          ),
                        ),
                      ),
                  ],
                ),
              ))
          .toList(),
    );
  }

  Widget _avaliacoesSecao(Empresa empresa) {
    if (_avaliacoesCarregando) {
      return const Padding(padding: EdgeInsets.all(16), child: Center(child: CircularProgressIndicator()));
    }
    final media = empresa.mediaAvaliacoes;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (media != null && (empresa.totalAvaliacoes ?? 0) > 0)
          Padding(
            padding: const EdgeInsets.only(bottom: 14),
            child: Row(
              children: [
                Text(media.toStringAsFixed(1).replaceAll('.', ','),
                    style: const TextStyle(fontSize: 40, fontWeight: FontWeight.w800, height: 1)),
                const SizedBox(width: 12),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Estrelas(nota: media, tamanho: 18, cor: AppCores.estrela),
                    const SizedBox(height: 4),
                    Text('${empresa.totalAvaliacoes} avaliações',
                        style: const TextStyle(color: AppCores.textoSecundario, fontWeight: FontWeight.w600)),
                  ],
                ),
              ],
            ),
          ),
        if (_avaliacoes.isEmpty)
          const Text('Ainda não há avaliações. Seja o primeiro a contar como foi!',
              style: TextStyle(color: AppCores.textoSecundario))
        else
          ..._avaliacoes.map(_cartaoAvaliacao),
      ],
    );
  }

  Widget _cartaoAvaliacao(Avaliacao avaliacao) {
    final inicial = avaliacao.nomeUsuario.isNotEmpty ? avaliacao.nomeUsuario[0].toUpperCase() : '?';
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(color: AppCores.creme, borderRadius: BorderRadius.circular(14)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              CircleAvatar(
                radius: 16,
                backgroundColor: AppCores.laranjaClaro,
                child: Text(inicial, style: const TextStyle(color: AppCores.laranjaEscuro, fontWeight: FontWeight.w800)),
              ),
              const SizedBox(width: 10),
              Expanded(child: Text(avaliacao.nomeUsuario, style: const TextStyle(fontWeight: FontWeight.w700))),
              Estrelas(nota: avaliacao.nota.toDouble(), tamanho: 14, cor: AppCores.estrela),
            ],
          ),
          if (_texto(avaliacao.comentario)) ...[
            const SizedBox(height: 8),
            Text(avaliacao.comentario!, style: const TextStyle(height: 1.4)),
          ],
        ],
      ),
    );
  }
}

/// WhatsApp (principal), ligar e rota: o caminho mais curto para o cliente falar com a empresa.
class _BarraDeAcoes extends StatelessWidget {
  const _BarraDeAcoes({required this.empresa, required this.aoAbrir});

  final Empresa empresa;
  final Future<void> Function(Uri? uri, {TipoEvento? evento}) aoAbrir;

  @override
  Widget build(BuildContext context) {
    final whatsapp = ContatoEmpresa.whatsapp(
      (empresa.whatsapp?.trim().isNotEmpty ?? false) ? empresa.whatsapp : null,
      nomeEmpresa: empresa.nome,
    );
    final telefone = ContatoEmpresa.telefone(empresa.telefone ?? empresa.whatsapp);
    final rota = empresa.temLocalizacao ? ContatoEmpresa.comoChegar(empresa.latitude!, empresa.longitude!) : null;
    if (whatsapp == null && telefone == null && rota == null) return const SizedBox.shrink();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (whatsapp != null)
          FilledButton.icon(
            style: FilledButton.styleFrom(backgroundColor: AppCores.whatsapp, minimumSize: const Size.fromHeight(54)),
            onPressed: () => aoAbrir(whatsapp, evento: TipoEvento.cliqueWhatsapp),
            icon: const Icon(Icons.chat_rounded),
            label: const Text('Chamar no WhatsApp'),
          ),
        if (telefone != null || rota != null) ...[
          const SizedBox(height: 10),
          Row(
            children: [
              if (telefone != null)
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => aoAbrir(telefone, evento: TipoEvento.cliqueLigar),
                    icon: const Icon(Icons.call_rounded, color: AppCores.laranja),
                    label: const Text('Ligar'),
                  ),
                ),
              if (telefone != null && rota != null) const SizedBox(width: 10),
              if (rota != null)
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => aoAbrir(rota, evento: TipoEvento.cliqueMapa),
                    icon: const Icon(Icons.directions_rounded, color: AppCores.laranja),
                    label: const Text('Como chegar'),
                  ),
                ),
            ],
          ),
        ],
      ],
    );
  }
}

/// Grade da semana com os horários; o dia de hoje fica destacado.
class _QuadroHorarios extends StatelessWidget {
  const _QuadroHorarios({required this.horarios});

  final List<Horario> horarios;

  @override
  Widget build(BuildContext context) {
    final hoje = DateTime.now().weekday;
    return Column(
      children: List.generate(7, (i) {
        final dia = i + 1;
        final doDia = horarios.where((h) => h.diaSemana == dia).toList()
          ..sort((a, b) => (a.abre.hour * 60 + a.abre.minute).compareTo(b.abre.hour * 60 + b.abre.minute));
        final ehHoje = dia == hoje;
        final estilo = TextStyle(
          fontWeight: ehHoje ? FontWeight.w800 : FontWeight.w500,
          color: ehHoje ? AppCores.laranjaEscuro : AppCores.marinho,
        );
        return Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          decoration: BoxDecoration(
            color: ehHoje ? AppCores.laranjaClaro.withValues(alpha: 0.6) : null,
            borderRadius: BorderRadius.circular(10),
          ),
          child: Row(
            children: [
              SizedBox(width: 90, child: Text(ehHoje ? 'Hoje' : nomeDia(dia), style: estilo)),
              Expanded(
                child: Text(
                  doDia.isEmpty
                      ? 'Fechado'
                      : doDia.map((h) => '${formatarHora(h.abre)} – ${formatarHora(h.fecha)}').join('  ·  '),
                  textAlign: TextAlign.right,
                  style: estilo.copyWith(color: doDia.isEmpty ? AppCores.textoSecundario : estilo.color),
                ),
              ),
            ],
          ),
        );
      }),
    );
  }
}

class _Secao extends StatelessWidget {
  const _Secao({required this.titulo, required this.icone, required this.child, this.acao});

  final String titulo;
  final IconData icone;
  final Widget child;
  final Widget? acao;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icone, size: 20, color: AppCores.laranja),
              const SizedBox(width: 8),
              Expanded(child: Text(titulo, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 17))),
              ?acao,
            ],
          ),
          const SizedBox(height: 10),
          child,
        ],
      ),
    );
  }
}
