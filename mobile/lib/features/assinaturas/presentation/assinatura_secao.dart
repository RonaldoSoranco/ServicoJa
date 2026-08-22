import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../../core/network/api_exception.dart';
import '../data/assinatura_repository.dart';
import '../models/assinatura.dart';

/// Secao "Assinatura Premium" embutida na tela de edicao de empresa (Fase 7).
///
/// E por empresa, nao por usuario: cada empresa do dono tem sua propria assinatura.
class AssinaturaSecao extends StatefulWidget {
  const AssinaturaSecao({super.key, required this.empresaId, this.aoMudarPremium});

  final int empresaId;

  /// Chamado quando o estado premium pode ter mudado (assinatura ativada/cancelada),
  /// para o formulario pai recarregar os dados da empresa (fotos/portfolio/destaque
  /// dependem de `premiumAtivo`).
  final VoidCallback? aoMudarPremium;

  @override
  State<AssinaturaSecao> createState() => _AssinaturaSecaoState();
}

class _AssinaturaSecaoState extends State<AssinaturaSecao> {
  final _repositorio = AssinaturaRepository();
  final _formatoData = DateFormat('dd/MM/yyyy', 'pt_BR');

  bool _carregando = true;
  bool _processando = false;
  Assinatura? _assinatura;
  String? _erro;

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
      final assinatura = await _repositorio.obterAtiva(widget.empresaId);
      if (mounted) setState(() => _assinatura = assinatura);
    } on ApiException catch (e) {
      if (mounted) setState(() => _erro = e.toString());
    } catch (_) {
      if (mounted) setState(() => _erro = 'Nao foi possivel carregar a assinatura.');
    } finally {
      if (mounted) setState(() => _carregando = false);
    }
  }

  Future<void> _assinar(TipoAssinatura tipo) async {
    setState(() => _processando = true);
    try {
      final assinatura = await _repositorio.criar(empresaId: widget.empresaId, tipo: tipo);
      if (mounted) setState(() => _assinatura = assinatura);
      if (assinatura.linkPagamento != null && assinatura.linkPagamento!.isNotEmpty) {
        final uri = Uri.tryParse(assinatura.linkPagamento!);
        if (uri != null) {
          await launchUrl(uri, mode: LaunchMode.externalApplication);
        }
      }
      widget.aoMudarPremium?.call();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(const SnackBar(content: Text('Nao foi possivel iniciar a assinatura.')));
      }
    } finally {
      if (mounted) setState(() => _processando = false);
    }
  }

  Future<void> _reabrirLinkPagamento() async {
    final link = _assinatura?.linkPagamento;
    if (link == null || link.isEmpty) return;
    final uri = Uri.tryParse(link);
    if (uri != null) await launchUrl(uri, mode: LaunchMode.externalApplication);
  }

  Future<void> _verificarPagamento() async {
    await _carregar();
    if (!mounted) return;
    if (_assinatura?.status == StatusAssinatura.ativa) {
      widget.aoMudarPremium?.call();
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Pagamento confirmado! Premium ativo.')));
    } else {
      ScaffoldMessenger.of(context)
          .showSnackBar(const SnackBar(content: Text('Ainda aguardando confirmacao do pagamento.')));
    }
  }

  Future<void> _cancelar() async {
    final assinatura = _assinatura;
    if (assinatura == null) return;
    final confirmar = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Cancelar assinatura'),
        content: const Text('Tem certeza que deseja cancelar a assinatura Premium desta empresa?'),
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Voltar')),
          FilledButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('Cancelar assinatura')),
        ],
      ),
    );
    if (confirmar != true) return;
    setState(() => _processando = true);
    try {
      await _repositorio.cancelar(assinatura.id);
      await _carregar();
      widget.aoMudarPremium?.call();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
    } finally {
      if (mounted) setState(() => _processando = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_carregando) {
      return const Padding(padding: EdgeInsets.all(16), child: Center(child: CircularProgressIndicator()));
    }
    if (_erro != null) {
      return Padding(
        padding: const EdgeInsets.symmetric(vertical: 12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(_erro!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
            TextButton(onPressed: _carregar, child: const Text('Tentar novamente')),
          ],
        ),
      );
    }

    final assinatura = _assinatura;
    if (assinatura == null || assinatura.status == StatusAssinatura.cancelada || assinatura.status == StatusAssinatura.expirada) {
      return _semAssinatura();
    }
    if (assinatura.status == StatusAssinatura.aguardandoPagamento) {
      return _aguardandoPagamento(assinatura);
    }
    // ATIVA ou ATRASADA: se premiumAtivo, tratamos como ativo; senao, oferecemos assinar de novo.
    if (assinatura.premiumAtivo) {
      return _assinaturaAtiva(assinatura);
    }
    return _semAssinatura();
  }

  Widget _semAssinatura() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Sua empresa ainda nao e Premium.', style: TextStyle(color: Colors.black54)),
        const SizedBox(height: 4),
        const Text(
          'Assinantes Premium podem adicionar fotos, portfolio e destacar a empresa nas buscas.',
          style: TextStyle(color: Colors.black45, fontSize: 12),
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(
              child: OutlinedButton(
                onPressed: _processando ? null : () => _assinar(TipoAssinatura.mensal),
                child: const Text('Assinar mensal'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: FilledButton(
                onPressed: _processando ? null : () => _assinar(TipoAssinatura.anual),
                child: const Text('Assinar anual'),
              ),
            ),
          ],
        ),
        if (_processando) ...[
          const SizedBox(height: 12),
          const Center(child: CircularProgressIndicator()),
        ],
      ],
    );
  }

  Widget _aguardandoPagamento(Assinatura assinatura) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: const [
            Icon(Icons.hourglass_top, color: Colors.orange),
            SizedBox(width: 8),
            Expanded(child: Text('Aguardando confirmacao do pagamento.')),
          ],
        ),
        const SizedBox(height: 8),
        Text('Plano: ${assinatura.tipo == TipoAssinatura.anual ? 'Anual' : 'Mensal'}'),
        if (assinatura.valor != null) Text('Valor: R\$ ${assinatura.valor!.toStringAsFixed(2)}'),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(
              child: OutlinedButton(onPressed: _reabrirLinkPagamento, child: const Text('Abrir link de pagamento')),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: FilledButton(
                onPressed: _processando ? null : _verificarPagamento,
                child: const Text('Ja paguei, verificar'),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _assinaturaAtiva(Assinatura assinatura) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            const Icon(Icons.workspace_premium, color: Colors.amber),
            const SizedBox(width: 8),
            Text('Premium ativo (${assinatura.tipo == TipoAssinatura.anual ? 'anual' : 'mensal'})',
                style: const TextStyle(fontWeight: FontWeight.bold)),
          ],
        ),
        const SizedBox(height: 8),
        if (assinatura.inicioEm != null) Text('Inicio: ${_formatoData.format(assinatura.inicioEm!)}'),
        if (assinatura.fimEm != null) Text('Renovacao/expira em: ${_formatoData.format(assinatura.fimEm!)}'),
        const SizedBox(height: 12),
        OutlinedButton(
          onPressed: _processando ? null : _cancelar,
          style: OutlinedButton.styleFrom(foregroundColor: Theme.of(context).colorScheme.error),
          child: const Text('Cancelar assinatura'),
        ),
      ],
    );
  }
}
