import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../categorias/data/categoria_repository.dart';
import '../../categorias/models/categoria.dart';
import '../../assinaturas/presentation/assinatura_secao.dart';
import '../data/empresa_repository.dart';
import '../models/empresa.dart';
import 'destaque_secao.dart';
import 'fotos_secao.dart';
import 'portfolio_secao.dart';

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
  late final TextEditingController _logoUrlController;
  late final TextEditingController _telefoneController;
  late final TextEditingController _whatsappController;
  late final TextEditingController _emailContatoController;
  late final TextEditingController _cepController;
  late final TextEditingController _enderecoController;
  late final TextEditingController _numeroController;
  late final TextEditingController _bairroController;
  late final TextEditingController _cidadeController;
  late final TextEditingController _ufController;
  late final TextEditingController _latitudeController;
  late final TextEditingController _longitudeController;
  late final TextEditingController _horarioController;
  late final TextEditingController _redesSociaisController;
  late final TextEditingController _siteController;

  int? _categoriaId;
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
    _logoUrlController = TextEditingController(text: e?.logoUrl ?? '');
    _telefoneController = TextEditingController(text: e?.telefone ?? '');
    _whatsappController = TextEditingController(text: e?.whatsapp ?? '');
    _emailContatoController = TextEditingController(text: e?.emailContato ?? '');
    _cepController = TextEditingController(text: e?.cep ?? '');
    _enderecoController = TextEditingController(text: e?.endereco ?? '');
    _numeroController = TextEditingController(text: e?.numero ?? '');
    _bairroController = TextEditingController(text: e?.bairro ?? '');
    _cidadeController = TextEditingController(text: e?.cidade ?? 'Marau');
    _ufController = TextEditingController(text: e?.uf ?? 'RS');
    _latitudeController = TextEditingController(text: e?.latitude?.toString() ?? '');
    _longitudeController = TextEditingController(text: e?.longitude?.toString() ?? '');
    _horarioController = TextEditingController(text: e?.horarioFuncionamento ?? '');
    _redesSociaisController = TextEditingController(text: e?.redesSociais ?? '');
    _siteController = TextEditingController(text: e?.site ?? '');
  }

  @override
  void dispose() {
    _nomeController.dispose();
    _descricaoCurtaController.dispose();
    _descricaoCompletaController.dispose();
    _logoUrlController.dispose();
    _telefoneController.dispose();
    _whatsappController.dispose();
    _emailContatoController.dispose();
    _cepController.dispose();
    _enderecoController.dispose();
    _numeroController.dispose();
    _bairroController.dispose();
    _cidadeController.dispose();
    _ufController.dispose();
    _latitudeController.dispose();
    _longitudeController.dispose();
    _horarioController.dispose();
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
    if (_editando && _empresaAtual!.aprovada) {
      final confirmar = await showDialog<bool>(
        context: context,
        builder: (_) => AlertDialog(
          title: const Text('Editar empresa aprovada'),
          content: const Text(
            'Sua empresa esta aprovada e visivel nas buscas. Ao salvar essa edicao, ela volta para analise '
            'e some da busca ate ser aprovada novamente pelo administrador. Deseja continuar?',
          ),
          actions: [
            TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Cancelar')),
            FilledButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('Continuar e salvar')),
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
      logoUrl: _logoUrlController.text.trim(),
      telefone: _telefoneController.text.trim(),
      whatsapp: _whatsappController.text.trim(),
      emailContato: _emailContatoController.text.trim(),
      cep: _cepController.text.trim(),
      endereco: _enderecoController.text.trim(),
      numero: _numeroController.text.trim(),
      bairro: _bairroController.text.trim(),
      cidade: _cidadeController.text.trim(),
      uf: _ufController.text.trim().toUpperCase(),
      latitude: double.tryParse(_latitudeController.text.trim().replaceAll(',', '.')),
      longitude: double.tryParse(_longitudeController.text.trim().replaceAll(',', '.')),
      horarioFuncionamento: _horarioController.text.trim(),
      redesSociais: _redesSociaisController.text.trim(),
      site: _siteController.text.trim(),
    );
    try {
      final resultado = _editando
          ? await _repositorio.atualizar(_empresaAtual!.id, payload)
          : await _repositorio.criar(payload);
      if (!mounted) return;
      setState(() => _empresaAtual = resultado);
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Empresa salva com sucesso.')));
      if (!_editando) {
        // Depois de criar, volta para a lista (que vai recarregar via /minhas).
        Navigator.of(context).pop(true);
      }
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

  Future<void> _adicionarFoto(String url, String? descricao) async {
    try {
      await _repositorio.adicionarFoto(_empresaAtual!.id, url: url, descricao: descricao);
      await _recarregarEmpresaAtual();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
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
                            Expanded(child: Text('Esta empresa esta pendente de aprovacao do administrador.')),
                          ],
                        ),
                      ),
                    _tituloSecao('Dados basicos'),
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
                      decoration: const InputDecoration(labelText: 'Descricao curta (aparece na lista de busca)'),
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _descricaoCompletaController,
                      maxLines: 4,
                      decoration: const InputDecoration(labelText: 'Descricao completa', alignLabelWithHint: true),
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _logoUrlController,
                      decoration: const InputDecoration(labelText: 'URL do logo (opcional)'),
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
                    _tituloSecao('Endereco'),
                    TextFormField(
                      controller: _cepController,
                      decoration: const InputDecoration(labelText: 'CEP'),
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _enderecoController,
                      decoration: const InputDecoration(labelText: 'Endereco (rua/avenida)'),
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: TextFormField(
                            controller: _numeroController,
                            decoration: const InputDecoration(labelText: 'Numero'),
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
                            validator: (v) => (v == null || v.trim().length != 2) ? 'UF invalida.' : null,
                          ),
                        ),
                      ],
                    ),
                    _tituloSecao('Avancado (opcional)'),
                    Row(
                      children: [
                        Expanded(
                          child: TextFormField(
                            controller: _latitudeController,
                            keyboardType: const TextInputType.numberWithOptions(signed: true, decimal: true),
                            decoration: const InputDecoration(labelText: 'Latitude'),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: TextFormField(
                            controller: _longitudeController,
                            keyboardType: const TextInputType.numberWithOptions(signed: true, decimal: true),
                            decoration: const InputDecoration(labelText: 'Longitude'),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _horarioController,
                      maxLines: 3,
                      decoration: const InputDecoration(
                        labelText: 'Horario de funcionamento',
                        alignLabelWithHint: true,
                        hintText: 'Ex.: Seg a sex, 8h as 18h',
                      ),
                    ),
                    const SizedBox(height: 12),
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
                          : Text(_editando ? 'Salvar alteracoes' : 'Cadastrar empresa'),
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
                      _tituloSecao('Portfolio'),
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

  Widget _tituloSecao(String titulo) {
    return Padding(
      padding: const EdgeInsets.only(top: 24, bottom: 12),
      child: Text(titulo, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
    );
  }
}

extension _PrimeiroOuNulo<T> on Iterable<T> {
  T? get firstOrNull => isEmpty ? null : first;
}
