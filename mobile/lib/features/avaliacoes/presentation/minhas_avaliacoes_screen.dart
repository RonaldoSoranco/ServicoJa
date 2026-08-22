import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/estado_erro.dart';
import '../../../core/widgets/estado_vazio.dart';
import '../../../core/widgets/estrelas.dart';
import '../data/avaliacao_repository.dart';
import '../models/avaliacao.dart';
import 'status_avaliacao_badge.dart';

class MinhasAvaliacoesScreen extends StatefulWidget {
  const MinhasAvaliacoesScreen({super.key});

  @override
  State<MinhasAvaliacoesScreen> createState() => _MinhasAvaliacoesScreenState();
}

class _MinhasAvaliacoesScreenState extends State<MinhasAvaliacoesScreen> {
  final _repositorio = AvaliacaoRepository();
  final _formatoData = DateFormat('dd/MM/yyyy', 'pt_BR');

  bool _carregando = true;
  String? _erro;
  List<Avaliacao> _avaliacoes = [];

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
      final avaliacoes = await _repositorio.listarMinhas();
      setState(() => _avaliacoes = avaliacoes);
    } on ApiException catch (e) {
      setState(() => _erro = e.toString());
    } catch (_) {
      setState(() => _erro = 'Ocorreu um erro inesperado. Tente novamente.');
    } finally {
      if (mounted) setState(() => _carregando = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Minhas avaliacoes')),
      body: _carregando
          ? const Center(child: CircularProgressIndicator())
          : _erro != null
              ? EstadoErro(mensagem: _erro!, aoTentarNovamente: _carregar)
              : _avaliacoes.isEmpty
                  ? const EstadoVazio(
                      mensagem: 'Voce ainda nao avaliou nenhuma empresa.',
                      icone: Icons.rate_review_outlined,
                    )
                  : RefreshIndicator(
                      onRefresh: _carregar,
                      child: ListView.builder(
                        padding: const EdgeInsets.all(16),
                        itemCount: _avaliacoes.length,
                        itemBuilder: (context, i) => _cartao(_avaliacoes[i]),
                      ),
                    ),
    );
  }

  Widget _cartao(Avaliacao avaliacao) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: Colors.grey.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Estrelas(nota: avaliacao.nota.toDouble(), tamanho: 16),
              const Spacer(),
              StatusAvaliacaoBadge(status: avaliacao.status),
            ],
          ),
          if (avaliacao.comentario != null && avaliacao.comentario!.isNotEmpty) ...[
            const SizedBox(height: 8),
            Text(avaliacao.comentario!),
          ],
          const SizedBox(height: 8),
          Text(_formatoData.format(avaliacao.criadoEm), style: const TextStyle(color: Colors.black45, fontSize: 12)),
          if (avaliacao.status == StatusAvaliacao.pendente) ...[
            const SizedBox(height: 4),
            const Text(
              'Sua avaliacao so aparece publicamente depois de aprovada por um administrador.',
              style: TextStyle(color: Colors.black45, fontSize: 12),
            ),
          ],
          if (avaliacao.status == StatusAvaliacao.rejeitada) ...[
            const SizedBox(height: 4),
            const Text(
              'Esta avaliacao foi rejeitada pela moderacao e nao aparece publicamente.',
              style: TextStyle(color: Colors.black45, fontSize: 12),
            ),
          ],
        ],
      ),
    );
  }
}
