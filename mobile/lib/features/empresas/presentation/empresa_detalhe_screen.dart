import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/estado_erro.dart';
import '../../../core/widgets/estrelas.dart';
import '../../auth/models/usuario.dart';
import '../../auth/state/auth_controller.dart';
import '../../avaliacoes/data/avaliacao_repository.dart';
import '../../avaliacoes/models/avaliacao.dart';
import '../../avaliacoes/presentation/avaliar_dialog.dart';
import '../../favoritos/data/favorito_repository.dart';
import '../data/empresa_repository.dart';
import '../models/empresa.dart';

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
    _carregar();
  }

  Future<void> _carregar() async {
    setState(() {
      _carregando = true;
      _erro = null;
    });
    try {
      final empresa = await _empresaRepositorio.detalhar(widget.empresaId);
      _empresa = empresa;
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
        const SnackBar(content: Text('Avaliacao enviada! Ela aparecera aqui assim que for aprovada.')),
      );
      _carregarAvaliacoes();
    }
  }

  Future<void> _abrirLink(String url) async {
    final uri = Uri.tryParse(url.startsWith('http') ? url : 'https://$url');
    if (uri == null) return;
    try {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Nao foi possivel abrir o link.')));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthController>();
    final ehCliente = auth.usuario?.perfil == Perfil.cliente;
    final empresa = _empresa;

    return Scaffold(
      appBar: AppBar(
        title: Text(empresa?.nome ?? 'Empresa'),
        actions: [
          if (ehCliente && empresa != null)
            IconButton(
              tooltip: _favoritado ? 'Remover dos favoritos' : 'Favoritar',
              icon: Icon(_favoritado ? Icons.favorite : Icons.favorite_border, color: _favoritado ? Colors.red : null),
              onPressed: _alternarFavorito,
            ),
        ],
      ),
      body: _carregando
          ? const Center(child: CircularProgressIndicator())
          : _erro != null
              ? EstadoErro(mensagem: _erro!, aoTentarNovamente: _carregar)
              : empresa == null
                  ? const SizedBox.shrink()
                  : RefreshIndicator(
                      onRefresh: _carregar,
                      child: ListView(
                        padding: const EdgeInsets.all(16),
                        children: [
                          _cabecalho(empresa),
                          const SizedBox(height: 16),
                          if (empresa.descricaoCurta != null && empresa.descricaoCurta!.isNotEmpty)
                            Text(empresa.descricaoCurta!, style: const TextStyle(fontSize: 15)),
                          if (empresa.descricaoCompleta != null && empresa.descricaoCompleta!.isNotEmpty) ...[
                            const SizedBox(height: 12),
                            _secao('Sobre', Text(empresa.descricaoCompleta!)),
                          ],
                          const SizedBox(height: 12),
                          _secao('Contato', _contato(empresa)),
                          _secao('Endereco', _endereco(empresa)),
                          if (empresa.horarioFuncionamento != null && empresa.horarioFuncionamento!.isNotEmpty)
                            _secao('Horario de funcionamento', Text(empresa.horarioFuncionamento!)),
                          if (empresa.redesSociais != null && empresa.redesSociais!.isNotEmpty)
                            _secao('Redes sociais', Text(empresa.redesSociais!)),
                          if (empresa.site != null && empresa.site!.isNotEmpty)
                            _secao(
                              'Site',
                              InkWell(
                                onTap: () => _abrirLink(empresa.site!),
                                child: Text(empresa.site!, style: const TextStyle(color: Colors.blue)),
                              ),
                            ),
                          if (empresa.fotos.isNotEmpty) _secaoFotos(empresa),
                          if (empresa.portfolios.isNotEmpty) _secaoPortfolio(empresa),
                          const Divider(height: 32),
                          _secaoAvaliacoes(empresa, ehCliente, auth),
                        ],
                      ),
                    ),
    );
  }

  Widget _cabecalho(Empresa empresa) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(12),
          child: empresa.logoUrl != null && empresa.logoUrl!.isNotEmpty
              ? Image.network(
                  empresa.logoUrl!,
                  width: 72,
                  height: 72,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => _logoPlaceholder(),
                )
              : _logoPlaceholder(),
        ),
        const SizedBox(width: 14),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(empresa.nome, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
              const SizedBox(height: 4),
              Text(empresa.categoria.nome, style: const TextStyle(color: Colors.black54)),
              const SizedBox(height: 4),
              Text('${empresa.cidade} - ${empresa.uf}', style: const TextStyle(color: Colors.black45, fontSize: 13)),
              const SizedBox(height: 6),
              Row(
                children: [
                  if (empresa.mediaAvaliacoes != null && (empresa.totalAvaliacoes ?? 0) > 0) ...[
                    Estrelas(nota: empresa.mediaAvaliacoes!, tamanho: 16),
                    const SizedBox(width: 6),
                    Text('${empresa.mediaAvaliacoes!.toStringAsFixed(1)} (${empresa.totalAvaliacoes})'),
                  ] else
                    const Text('Sem avaliacoes ainda', style: TextStyle(color: Colors.black38, fontSize: 13)),
                  if (empresa.destaque) ...[
                    const SizedBox(width: 8),
                    Icon(Icons.star_rounded, size: 16, color: Theme.of(context).colorScheme.secondary),
                  ],
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _logoPlaceholder() {
    return Container(
      width: 72,
      height: 72,
      color: Colors.grey.shade100,
      child: const Icon(Icons.storefront_outlined, color: Colors.black26, size: 32),
    );
  }

  Widget _secao(String titulo, Widget conteudo) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(titulo, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
          const SizedBox(height: 6),
          conteudo,
        ],
      ),
    );
  }

  Widget _contato(Empresa empresa) {
    final linhas = <Widget>[];
    if (empresa.telefone != null && empresa.telefone!.isNotEmpty) {
      linhas.add(_linhaContato(Icons.call_outlined, empresa.telefone!));
    }
    if (empresa.whatsapp != null && empresa.whatsapp!.isNotEmpty) {
      linhas.add(_linhaContato(Icons.chat_outlined, empresa.whatsapp!));
    }
    if (empresa.emailContato != null && empresa.emailContato!.isNotEmpty) {
      linhas.add(_linhaContato(Icons.email_outlined, empresa.emailContato!));
    }
    if (linhas.isEmpty) return const Text('Nao informado.', style: TextStyle(color: Colors.black45));
    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: linhas);
  }

  Widget _linhaContato(IconData icone, String texto) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 4),
      child: Row(
        children: [
          Icon(icone, size: 16, color: Colors.black54),
          const SizedBox(width: 8),
          Expanded(child: Text(texto)),
        ],
      ),
    );
  }

  Widget _endereco(Empresa empresa) {
    final partes = [
      if (empresa.endereco != null && empresa.endereco!.isNotEmpty) empresa.endereco,
      if (empresa.numero != null && empresa.numero!.isNotEmpty) empresa.numero,
      if (empresa.bairro != null && empresa.bairro!.isNotEmpty) empresa.bairro,
    ].whereType<String>().join(', ');
    final linha2 = [
      empresa.cidade,
      empresa.uf,
      if (empresa.cep != null && empresa.cep!.isNotEmpty) empresa.cep,
    ].whereType<String>().join(' - ');
    return Text(partes.isEmpty ? linha2 : '$partes\n$linha2');
  }

  Widget _secaoFotos(Empresa empresa) {
    return _secao(
      'Fotos',
      SizedBox(
        height: 90,
        child: ListView.separated(
          scrollDirection: Axis.horizontal,
          itemCount: empresa.fotos.length,
          separatorBuilder: (_, __) => const SizedBox(width: 8),
          itemBuilder: (context, i) {
            final foto = empresa.fotos[i];
            return ClipRRect(
              borderRadius: BorderRadius.circular(10),
              child: Image.network(
                foto.url,
                width: 90,
                height: 90,
                fit: BoxFit.cover,
                errorBuilder: (_, __, ___) => Container(
                  width: 90,
                  height: 90,
                  color: Colors.grey.shade100,
                  child: const Icon(Icons.broken_image_outlined, color: Colors.black26),
                ),
              ),
            );
          },
        ),
      ),
    );
  }

  Widget _secaoPortfolio(Empresa empresa) {
    return _secao(
      'Portfolio',
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: empresa.portfolios
            .map((p) => Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (p.titulo != null && p.titulo!.isNotEmpty)
                        Text(p.titulo!, style: const TextStyle(fontWeight: FontWeight.w600)),
                      if (p.descricao != null && p.descricao!.isNotEmpty) Text(p.descricao!),
                      if (p.urlMidia != null && p.urlMidia!.isNotEmpty)
                        Padding(
                          padding: const EdgeInsets.only(top: 4),
                          child: ClipRRect(
                            borderRadius: BorderRadius.circular(10),
                            child: Image.network(
                              p.urlMidia!,
                              height: 120,
                              fit: BoxFit.cover,
                              errorBuilder: (_, __, ___) => Container(
                                height: 120,
                                color: Colors.grey.shade100,
                                child: const Icon(Icons.broken_image_outlined, color: Colors.black26),
                              ),
                            ),
                          ),
                        ),
                    ],
                  ),
                ))
            .toList(),
      ),
    );
  }

  Widget _secaoAvaliacoes(Empresa empresa, bool ehCliente, AuthController auth) {
    // Apenas usuarios com perfil CLIENTE podem avaliar (regra do backend); donos de
    // empresa tem perfil EMPRESA, entao ja ficam de fora sem precisar de outra checagem.
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            const Expanded(
              child: Text('Avaliacoes', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            ),
            if (ehCliente) TextButton(onPressed: _avaliar, child: const Text('Avaliar')),
          ],
        ),
        const SizedBox(height: 8),
        if (_avaliacoesCarregando)
          const Padding(padding: EdgeInsets.all(16), child: Center(child: CircularProgressIndicator()))
        else if (_avaliacoes.isEmpty)
          const Padding(
            padding: EdgeInsets.symmetric(vertical: 12),
            child: Text('Nenhuma avaliacao aprovada ainda.', style: TextStyle(color: Colors.black45)),
          )
        else
          ..._avaliacoes.map(_cartaoAvaliacao),
      ],
    );
  }

  Widget _cartaoAvaliacao(Avaliacao avaliacao) {
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: Colors.grey.shade50, borderRadius: BorderRadius.circular(10)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(child: Text(avaliacao.nomeUsuario, style: const TextStyle(fontWeight: FontWeight.w600))),
              Estrelas(nota: avaliacao.nota.toDouble(), tamanho: 14),
            ],
          ),
          if (avaliacao.comentario != null && avaliacao.comentario!.isNotEmpty) ...[
            const SizedBox(height: 6),
            Text(avaliacao.comentario!),
          ],
        ],
      ),
    );
  }
}
