import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:latlong2/latlong.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/mapa.dart';
import '../../categorias/data/categoria_repository.dart';
import '../../categorias/models/categoria.dart';
import '../../assinaturas/presentation/assinatura_secao.dart';
import '../data/empresa_repository.dart';
import '../models/empresa.dart';
import '../models/horario.dart';
import 'destaque_secao.dart';
import 'editor_horarios.dart';
import 'fotos_secao.dart';
import 'portfolio_secao.dart';
import 'seletor_localizacao_screen.dart';

/// Formulario de cadastro/edicao de empresa (Fase 6).
///
/// Quando `empresa` e informado, tambem exibe as secoes de Assinatura (Fase 7)
/// e Fotos/Portfolio/Destaque premium-gated (Fase 8), pois essas dependem de um
/// id de empresa ja existente.
class EmpresaFormularioScreen extends StatefulWidget {
  const EmpresaFormularioScreen({super.key, this.empresa});

  final Empresa? empresa;

  @override
  State<EmpresaFormularioScreen> createState() => _EmpresaFormularioScreenState();
}

class _EmpresaFormularioScreenState extends State<EmpresaFormularioScreen> {
  final _formKey = GlobalKey<FormState>();
  final _repositorio = EmpresaRepository();
  late final Future<List<Categoria>> _categoriasFuture;

  late final TextEditingController _nomeController;
  late final TextEditingController _descricaoCurtaController;
  late final TextEditingController _descricaoCompletaController;
  late final TextEditingController _telefoneController;
  late final TextEditingController _whatsappController;
  late final TextEditingController _emailContatoController;
  late final TextEditingController _cepController;
  late final TextEditingController _enderecoController;
  late final TextEditingController _numeroController;
  late final TextEditingController _bairroController;
  late final TextEditingController _cidadeController;
  late final TextEditingController _ufController;
  late final TextEditingController _redesSociaisController;
  late final TextEditingController _siteController;

  int? _categoriaId;
  LatLng? _localizacao;
  List<Horario> _horarios = [];
  bool _salvando = false;
  Empresa? _empresaAtual;

  bool get _editando => _empresaAtual != null;

  @override
  void initState() {
    super.initState();
    _empresaAtual = widget.empresa;
    final e = widget.empresa;
    _categoriasFuture = CategoriaRepository().listarAtivas();
    _categoriaId = e?.categoria.id;
    _nomeController = TextEditingController(text: e?.nome ?? '');
    _descricaoCurtaController = TextEditingController(text: e?.descricaoCurta ?? '');
    _descricaoCompletaController = TextEditingController(text: e?.descricaoCompleta ?? '');
    _telefoneController = TextEditingController(text: e?.telefone ?? '');
    _whatsappController = TextEditingController(text: e?.whatsapp ?? '');
    _emailContatoController = TextEditingController(text: e?.emailContato ?? '');
    _cepController = TextEditingController(text: e?.cep ?? '');
    _enderecoController = TextEditingController(text: e?.endereco ?? '');
    _numeroController = TextEditingController(text: e?.numero ?? '');
    _bairroController = TextEditingController(text: e?.bairro ?? '');
    _cidadeController = TextEditingController(text: e?.cidade ?? 'Marau');
    _ufController = TextEditingController(text: e?.uf ?? 'RS');
    _localizacao = (e != null && e.temLocalizacao) ? LatLng(e.latitude!, e.longitude!) : null;
    _horarios = [...?e?.horarios];
    _redesSociaisController = TextEditingController(text: e?.redesSociais ?? '');
    _siteController = TextEditingController(text: e?.site ?? '');
  }

  @override
  void dispose() {
    _nomeController.dispose();
    _descricaoCurtaController.dispose();
    _descricaoCompletaController.dispose();
    _telefoneController.dispose();
    _whatsappController.dispose();
    _emailContatoController.dispose();
    _cepController.dispose();
    _enderecoController.dispose();
    _numeroController.dispose();
    _bairroController.dispose();
    _cidadeController.dispose();
    _ufController.dispose();
    _redesSociaisController.dispose();
    _siteController.dispose();
    super.dispose();
  }

  Future<void> _recarregarEmpresaAtual() async {
    if (_empresaAtual == null) return;
    try {
      final minhas = await _repositorio.listarMinhas();
      final atualizada = minhas.where((emp) => emp.id == _empresaAtual!.id).firstOrNull;
      if (atualizada != null && mounted) setState(() => _empresaAtual = atualizada);
    } catch (_) {
      // Mantem os dados atuais em caso de falha pontual ao recarregar.
    }
  }

  Future<void> _salvar() async {
    if (!_formKey.currentState!.validate()) return;
    if (_categoriaId == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Selecione uma categoria.')));
      return;
    }
    if (_editando && _empresaAtual!.aprovada && _alterouConteudoModerado()) {
      final confirmar = await showDialog<bool>(
        context: context,
        builder: (_) => AlertDialog(
          title: const Text('Enviar para nova análise?'),
          content: const Text(
            'Você alterou nome, categoria, descrição ou contatos. Sua empresa sai da busca até o administrador '
            'aprovar de novo. Horários, endereço e localização podem ser alterados sem nova análise.',
          ),
          actions: [
            TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Cancelar')),
            FilledButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('Salvar mesmo assim')),
          ],
        ),
      );
      if (confirmar != true) return;
    }

    setState(() => _salvando = true);
    final payload = EmpresaRequestPayload(
      nome: _nomeController.text.trim(),
      categoriaId: _categoriaId!,
      descricaoCurta: _descricaoCurtaController.text.trim(),
      descricaoCompleta: _descricaoCompletaController.text.trim(),
      telefone: _telefoneController.text.trim(),
      whatsapp: _whatsappController.text.trim(),
      emailContato: _emailContatoController.text.trim(),
      cep: _cepController.text.trim(),
      endereco: _enderecoController.text.trim(),
      numero: _numeroController.text.trim(),
      bairro: _bairroController.text.trim(),
      cidade: _cidadeController.text.trim(),
      uf: _ufController.text.trim().toUpperCase(),
      latitude: _localizacao?.latitude,
      longitude: _localizacao?.longitude,
      horarios: _horarios,
      redesSociais: _redesSociaisController.text.trim(),
      site: _siteController.text.trim(),
    );
    final criando = !_editando;
    try {
      final resultado = criando
          ? await _repositorio.criar(payload)
          : await _repositorio.atualizar(_empresaAtual!.id, payload);
      if (!mounted) return;
      // Depois de criar, continua na tela para a empresa enviar o logo e ver as secoes Premium.
      setState(() => _empresaAtual = resultado);
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text(criando
            ? 'Empresa cadastrada! Envie o logo e ela aparece na busca depois da aprovação.'
            : 'Empresa salva com sucesso.'),
      ));
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(const SnackBar(content: Text('Ocorreu um erro inesperado. Tente novamente.')));
      }
    } finally {
      if (mounted) setState(() => _salvando = false);
    }
  }

  Future<void> _adicionarFoto(List<int> bytes, String nomeArquivo, String? descricao) async {
    try {
      await _repositorio.enviarFoto(_empresaAtual!.id, bytes: bytes, nomeArquivo: nomeArquivo, descricao: descricao);
      await _recarregarEmpresaAtual();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
    }
  }

  bool _enviandoLogo = false;

  Future<void> _enviarLogo() async {
    final arquivo = await ImagePicker().pickImage(source: ImageSource.gallery, imageQuality: 85);
    if (arquivo == null) return;
    setState(() => _enviandoLogo = true);
    try {
      final bytes = await arquivo.readAsBytes();
      final atualizada = await _repositorio.enviarLogo(_empresaAtual!.id, bytes: bytes, nomeArquivo: arquivo.name);
      if (mounted) setState(() => _empresaAtual = atualizada);
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
    } finally {
      if (mounted) setState(() => _enviandoLogo = false);
    }
  }

  Future<void> _removerFoto(Foto foto) async {
    try {
      await _repositorio.removerFoto(_empresaAtual!.id, foto.id);
      await _recarregarEmpresaAtual();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
    }
  }

  Future<void> _adicionarPortfolio(String? titulo, String? descricao, String? urlMidia) async {
    try {
      await _repositorio.adicionarPortfolio(_empresaAtual!.id, titulo: titulo, descricao: descricao, urlMidia: urlMidia);
      await _recarregarEmpresaAtual();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
    }
  }

  Future<void> _removerPortfolio(Portfolio portfolio) async {
    try {
      await _repositorio.removerPortfolio(_empresaAtual!.id, portfolio.id);
      await _recarregarEmpresaAtual();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
    }
  }

  bool _destaqueProcessando = false;

  Future<void> _alternarDestaque(bool ativar) async {
    setState(() => _destaqueProcessando = true);
    try {
      if (ativar) {
        await _repositorio.ativarDestaque(_empresaAtual!.id);
      } else {
        await _repositorio.removerDestaque(_empresaAtual!.id);
      }
      await _recarregarEmpresaAtual();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
    } finally {
      if (mounted) setState(() => _destaqueProcessando = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(_editando ? 'Editar empresa' : 'Nova empresa')),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(20),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 560),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    if (_editando && !_empresaAtual!.aprovada)
                      Container(
                        margin: const EdgeInsets.only(bottom: 16),
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(color: Colors.orange.shade50, borderRadius: BorderRadius.circular(10)),
                        child: const Row(
                          children: [
                            Icon(Icons.hourglass_top, color: Colors.orange),
                            SizedBox(width: 8),
                            Expanded(child: Text('Esta empresa está aguardando a aprovação do administrador.')),
                          ],
                        ),
                      ),
                    _tituloSecao('Dados básicos'),
                    TextFormField(
                      controller: _nomeController,
                      decoration: const InputDecoration(labelText: 'Nome da empresa'),
                      validator: (v) => (v == null || v.trim().isEmpty) ? 'Informe o nome da empresa.' : null,
                    ),
                    const SizedBox(height: 12),
                    FutureBuilder<List<Categoria>>(
                      future: _categoriasFuture,
                      builder: (context, snapshot) {
                        final categorias = snapshot.data ?? const <Categoria>[];
                        return DropdownButtonFormField<int>(
                          initialValue: _categoriaId,
                          decoration: const InputDecoration(labelText: 'Categoria'),
                          items: categorias.map((c) => DropdownMenuItem(value: c.id, child: Text(c.nome))).toList(),
                          onChanged: (v) => setState(() => _categoriaId = v),
                        );
                      },
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _descricaoCurtaController,
                      maxLength: 255,
                      decoration: const InputDecoration(labelText: 'Descrição curta (aparece na busca)'),
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _descricaoCompletaController,
                      maxLines: 4,
                      decoration: const InputDecoration(labelText: 'Descrição completa', alignLabelWithHint: true),
                    ),
                    const SizedBox(height: 16),
                    _CampoLogo(
                      logoUrl: _empresaAtual?.logoUrl,
                      habilitado: _editando,
                      enviando: _enviandoLogo,
                      aoEnviar: _enviarLogo,
                    ),
                    _tituloSecao('Contato'),
                    TextFormField(
                      controller: _telefoneController,
                      keyboardType: TextInputType.phone,
                      decoration: const InputDecoration(labelText: 'Telefone'),
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _whatsappController,
                      keyboardType: TextInputType.phone,
                      decoration: const InputDecoration(labelText: 'WhatsApp'),
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _emailContatoController,
                      keyboardType: TextInputType.emailAddress,
                      decoration: const InputDecoration(labelText: 'E-mail de contato'),
                    ),
                    _tituloSecao('Endereço'),
                    TextFormField(
                      controller: _cepController,
                      decoration: const InputDecoration(labelText: 'CEP'),
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _enderecoController,
                      decoration: const InputDecoration(labelText: 'Endereço (rua/avenida)'),
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: TextFormField(
                            controller: _numeroController,
                            decoration: const InputDecoration(labelText: 'Número'),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          flex: 2,
                          child: TextFormField(
                            controller: _bairroController,
                            decoration: const InputDecoration(labelText: 'Bairro'),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          flex: 2,
                          child: TextFormField(
                            controller: _cidadeController,
                            decoration: const InputDecoration(labelText: 'Cidade'),
                            validator: (v) => (v == null || v.trim().isEmpty) ? 'Informe a cidade.' : null,
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: TextFormField(
                            controller: _ufController,
                            maxLength: 2,
                            textCapitalization: TextCapitalization.characters,
                            decoration: const InputDecoration(labelText: 'UF', counterText: ''),
                            validator: (v) => (v == null || v.trim().length != 2) ? 'UF inválida.' : null,
                          ),
                        ),
                      ],
                    ),
                    _tituloSecao('Localização no mapa'),
                    _campoLocalizacao(),
                    _tituloSecao('Horário de funcionamento'),
                    const Text(
                      'Com os horários preenchidos, sua empresa mostra "Aberto agora" para os clientes.',
                      style: TextStyle(color: AppCores.textoSecundario),
                    ),
                    const SizedBox(height: 8),
                    EditorHorarios(horarios: _horarios, aoAlterar: (novos) => setState(() => _horarios = novos)),
                    _tituloSecao('Redes sociais e site'),
                    TextFormField(
                      controller: _redesSociaisController,
                      maxLines: 3,
                      decoration: const InputDecoration(labelText: 'Redes sociais', alignLabelWithHint: true),
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _siteController,
                      decoration: const InputDecoration(labelText: 'Site'),
                    ),
                    const SizedBox(height: 24),
                    ElevatedButton(
                      onPressed: _salvando ? null : _salvar,
                      child: _salvando
                          ? const SizedBox(
                              height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                          : Text(_editando ? 'Salvar alterações' : 'Cadastrar empresa'),
                    ),
                    if (_editando) ...[
                      _tituloSecao('Assinatura Premium'),
                      AssinaturaSecao(empresaId: _empresaAtual!.id, aoMudarPremium: _recarregarEmpresaAtual),
                      _tituloSecao('Fotos'),
                      FotosSecao(
                        premiumAtivo: _empresaAtual!.premiumAtivo,
                        fotos: _empresaAtual!.fotos,
                        aoAdicionar: _adicionarFoto,
                        aoRemover: _removerFoto,
                      ),
                      _tituloSecao('Portfólio'),
                      PortfolioSecao(
                        premiumAtivo: _empresaAtual!.premiumAtivo,
                        portfolios: _empresaAtual!.portfolios,
                        aoAdicionar: _adicionarPortfolio,
                        aoRemover: _removerPortfolio,
                      ),
                      _tituloSecao('Destaque'),
                      DestaqueSecao(
                        premiumAtivo: _empresaAtual!.premiumAtivo,
                        aprovada: _empresaAtual!.aprovada,
                        destaque: _empresaAtual!.destaque,
                        processando: _destaqueProcessando,
                        aoAlternar: _alternarDestaque,
                      ),
                    ],
                    const SizedBox(height: 24),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  /// Mesmos campos que o backend modera: mudar qualquer um deles tira a empresa da busca.
  bool _alterouConteudoModerado() {
    final e = _empresaAtual!;
    bool mudou(TextEditingController c, String? original) => c.text.trim() != (original ?? '').trim();
    return _categoriaId != e.categoria.id ||
        mudou(_nomeController, e.nome) ||
        mudou(_descricaoCurtaController, e.descricaoCurta) ||
        mudou(_descricaoCompletaController, e.descricaoCompleta) ||
        mudou(_telefoneController, e.telefone) ||
        mudou(_whatsappController, e.whatsapp) ||
        mudou(_emailContatoController, e.emailContato) ||
        mudou(_redesSociaisController, e.redesSociais) ||
        mudou(_siteController, e.site);
  }

  Future<void> _escolherLocalizacao() async {
    final escolhida = await Navigator.of(context).push<LatLng>(
      MaterialPageRoute(builder: (_) => SeletorLocalizacaoScreen(inicial: _localizacao)),
    );
    if (escolhida != null) setState(() => _localizacao = escolhida);
  }

  Widget _campoLocalizacao() {
    if (_localizacao == null) {
      return OutlinedButton.icon(
        onPressed: _escolherLocalizacao,
        icon: const Icon(Icons.add_location_alt_outlined, color: AppCores.laranja),
        label: const Text('Marcar a empresa no mapa'),
      );
    }
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        MapaPrevia(ponto: _localizacao!, altura: 150, aoTocar: _escolherLocalizacao),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: TextButton.icon(
                onPressed: _escolherLocalizacao,
                icon: const Icon(Icons.edit_location_alt_outlined),
                label: const Text('Alterar no mapa'),
              ),
            ),
            TextButton(
              onPressed: () => setState(() => _localizacao = null),
              style: TextButton.styleFrom(foregroundColor: AppCores.textoSecundario),
              child: const Text('Remover'),
            ),
          ],
        ),
      ],
    );
  }

  Widget _tituloSecao(String titulo) {
    return Padding(
      padding: const EdgeInsets.only(top: 28, bottom: 12),
      child: Text(titulo, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 17)),
    );
  }
}

/// Logo da empresa: so pode ser trocado pelo upload da imagem, que fica disponivel
/// depois que a empresa e cadastrada (o arquivo e gravado na pasta da empresa).
class _CampoLogo extends StatelessWidget {
  const _CampoLogo({
    required this.logoUrl,
    required this.habilitado,
    required this.enviando,
    required this.aoEnviar,
  });

  final String? logoUrl;
  final bool habilitado;
  final bool enviando;
  final VoidCallback aoEnviar;

  bool get _temLogo => logoUrl != null && logoUrl!.isNotEmpty;

  @override
  Widget build(BuildContext context) {
    if (!habilitado) {
      return const Text(
        'Você poderá enviar o logo depois de cadastrar a empresa.',
        style: TextStyle(color: Colors.black54),
      );
    }
    return Row(
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(12),
          child: _temLogo
              ? Image.network(
                  logoUrl!,
                  width: 64,
                  height: 64,
                  fit: BoxFit.cover,
                  errorBuilder: (context, error, stack) => _semLogo(Icons.broken_image_outlined),
                )
              : _semLogo(Icons.storefront_outlined),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: OutlinedButton.icon(
            onPressed: enviando ? null : aoEnviar,
            icon: enviando
                ? const SizedBox(height: 18, width: 18, child: CircularProgressIndicator(strokeWidth: 2))
                : const Icon(Icons.upload_outlined),
            label: Text(_temLogo ? 'Trocar logo' : 'Enviar logo'),
          ),
        ),
      ],
    );
  }

  Widget _semLogo(IconData icone) {
    return Container(
      width: 64,
      height: 64,
      color: Colors.grey.shade100,
      child: Icon(icone, color: Colors.black26),
    );
  }
}

extension _PrimeiroOuNulo<T> on Iterable<T> {
  T? get firstOrNull => isEmpty ? null : first;
}
