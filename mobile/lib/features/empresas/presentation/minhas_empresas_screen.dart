import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/estado_erro.dart';
import '../../../core/widgets/estado_vazio.dart';
import '../../../core/widgets/selo.dart';
import '../../categorias/presentation/visual_categoria.dart';
import '../data/empresa_repository.dart';
import '../models/empresa.dart';
import 'empresa_formulario_screen.dart';
import 'widgets/painel_desempenho.dart';

class MinhasEmpresasScreen extends StatefulWidget {
  const MinhasEmpresasScreen({super.key});

  @override
  State<MinhasEmpresasScreen> createState() => _MinhasEmpresasScreenState();
}

class _MinhasEmpresasScreenState extends State<MinhasEmpresasScreen> {
  final _repositorio = EmpresaRepository();
  bool _carregando = true;
  String? _erro;
  List<Empresa> _empresas = [];

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
      final empresas = await _repositorio.listarMinhas();
      if (mounted) setState(() => _empresas = empresas);
    } on ApiException catch (e) {
      if (mounted) setState(() => _erro = e.toString());
    } catch (_) {
      if (mounted) setState(() => _erro = 'Ocorreu um erro inesperado. Tente novamente.');
    } finally {
      if (mounted) setState(() => _carregando = false);
    }
  }

  Future<void> _abrirFormulario({Empresa? empresa}) async {
    await Navigator.of(context).push(
      MaterialPageRoute(builder: (_) => EmpresaFormularioScreen(empresa: empresa)),
    );
    _carregar();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Minha empresa')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _abrirFormulario(),
        icon: const Icon(Icons.add),
        label: const Text('Nova empresa'),
      ),
      body: _carregando
          ? const Center(child: CircularProgressIndicator())
          : _erro != null
              ? EstadoErro(mensagem: _erro!, aoTentarNovamente: _carregar)
              : _empresas.isEmpty
                  ? EstadoVazio(
                      mensagem: 'Você ainda não cadastrou nenhuma empresa.',
                      icone: Icons.storefront_outlined,
                      acao: () => _abrirFormulario(),
                      rotuloAcao: 'Cadastrar empresa',
                    )
                  : RefreshIndicator(
                      onRefresh: _carregar,
                      child: ListView.builder(
                        padding: const EdgeInsets.fromLTRB(16, 16, 16, 88),
                        itemCount: _empresas.length,
                        itemBuilder: (context, i) => _cartao(_empresas[i]),
                      ),
                    ),
    );
  }

  Widget _cartao(Empresa empresa) {
    final pendencias = [
      if (empresa.logoUrl == null || empresa.logoUrl!.isEmpty) 'logo',
      if (!empresa.temLocalizacao) 'localização no mapa',
      if (empresa.horarios.isEmpty) 'horários',
    ];
    return Card(
      margin: const EdgeInsets.only(bottom: 14),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: () => _abrirFormulario(empresa: empresa),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  LogoEmpresa(url: empresa.logoUrl, chaveCategoria: empresa.categoria.icone, tamanho: 52),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(empresa.nome, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 16)),
                        const SizedBox(height: 2),
                        Text(empresa.categoria.nome, style: const TextStyle(color: AppCores.textoSecundario)),
                      ],
                    ),
                  ),
                  const Icon(Icons.edit_outlined, color: AppCores.textoSecundario),
                ],
              ),
              const SizedBox(height: 12),
              Wrap(
                spacing: 6,
                runSpacing: 6,
                children: [
                  empresa.aprovada
                      ? const Selo(texto: 'Na busca', cor: AppCores.verde, icone: Icons.check_circle_rounded)
                      : const Selo(texto: 'Em análise', cor: AppCores.laranjaEscuro, icone: Icons.hourglass_top_rounded),
                  if (empresa.premiumAtivo)
                    const Selo(texto: 'Premium', cor: Color(0xFFB45309), icone: Icons.workspace_premium_rounded),
                  if (empresa.destaque) const Selo(texto: 'Destaque', cor: AppCores.laranja, icone: Icons.star_rounded),
                  SeloFuncionamento(aberto: empresa.abertoAgora, temHorarios: empresa.horarios.isNotEmpty),
                ],
              ),
              if (pendencias.isNotEmpty) ...[
                const SizedBox(height: 12),
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(color: AppCores.creme, borderRadius: BorderRadius.circular(12)),
                  child: Row(
                    children: [
                      const Icon(Icons.tips_and_updates_outlined, color: AppCores.laranja, size: 20),
                      const SizedBox(width: 10),
                      Expanded(child: Text('Complete o perfil: adicione ${pendencias.join(', ')}.')),
                    ],
                  ),
                ),
              ],
              const Divider(height: 28),
              if (empresa.aprovada)
                PainelDesempenho(empresaId: empresa.id)
              else
                const Text(
                  'O painel de desempenho começa a contar quando a empresa aparecer na busca.',
                  style: TextStyle(color: AppCores.textoSecundario),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
