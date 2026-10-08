import 'package:flutter/material.dart';

import '../../../../core/theme/app_theme.dart';
import '../../data/empresa_repository.dart';
import '../../models/desempenho.dart';

/// Quantas pessoas viram o perfil e chamaram a empresa nos últimos 30 dias.
class PainelDesempenho extends StatefulWidget {
  const PainelDesempenho({super.key, required this.empresaId, this.repositorio});

  final int empresaId;
  final EmpresaRepository? repositorio;

  @override
  State<PainelDesempenho> createState() => _PainelDesempenhoState();
}

class _PainelDesempenhoState extends State<PainelDesempenho> {
  late final Future<Desempenho> _desempenho =
      (widget.repositorio ?? EmpresaRepository()).desempenho(widget.empresaId);

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<Desempenho>(
      future: _desempenho,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const Padding(
            padding: EdgeInsets.symmetric(vertical: 20),
            child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
          );
        }
        final desempenho = snapshot.data;
        if (desempenho == null) {
          return const Text('Não foi possível carregar o desempenho agora.',
              style: TextStyle(color: AppCores.textoSecundario));
        }
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Últimos ${desempenho.dias} dias',
                style: const TextStyle(fontWeight: FontWeight.w800, color: AppCores.textoSecundario, fontSize: 12.5)),
            const SizedBox(height: 10),
            LayoutBuilder(
              builder: (context, restricoes) {
                final largura = (restricoes.maxWidth - 10) / 2;
                return Wrap(
                  spacing: 10,
                  runSpacing: 10,
                  children: [
                    _Indicador(largura: largura, rotulo: 'Visualizações', icone: Icons.visibility_rounded,
                        cor: AppCores.laranja, metrica: desempenho.visualizacoes),
                    _Indicador(largura: largura, rotulo: 'WhatsApp', icone: Icons.chat_rounded,
                        cor: AppCores.whatsapp, metrica: desempenho.cliquesWhatsapp),
                    _Indicador(largura: largura, rotulo: 'Ligações', icone: Icons.call_rounded,
                        cor: const Color(0xFF2563EB), metrica: desempenho.cliquesLigar),
                    _Indicador(largura: largura, rotulo: 'Rotas no mapa', icone: Icons.directions_rounded,
                        cor: const Color(0xFF7C3AED), metrica: desempenho.cliquesMapa),
                  ],
                );
              },
            ),
            if (desempenho.visualizacoes.atual > 0 && desempenho.totalContatos == 0) ...[
              const SizedBox(height: 10),
              const Text(
                'Dica: perfis com logo, horários e localização no mapa recebem mais contatos.',
                style: TextStyle(color: AppCores.textoSecundario, fontSize: 12.5),
              ),
            ],
          ],
        );
      },
    );
  }
}

class _Indicador extends StatelessWidget {
  const _Indicador({
    required this.largura,
    required this.rotulo,
    required this.icone,
    required this.cor,
    required this.metrica,
  });

  final double largura;
  final String rotulo;
  final IconData icone;
  final Color cor;
  final Metrica metrica;

  @override
  Widget build(BuildContext context) {
    final variacao = metrica.variacaoPercentual;
    return Container(
      width: largura,
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: cor.withValues(alpha: 0.08), borderRadius: BorderRadius.circular(14)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icone, size: 18, color: cor),
              const Spacer(),
              if (variacao != null)
                Text(
                  '${variacao >= 0 ? '+' : ''}$variacao%',
                  style: TextStyle(
                    fontSize: 11.5,
                    fontWeight: FontWeight.w800,
                    color: variacao >= 0 ? AppCores.verde : const Color(0xFFDC2626),
                  ),
                ),
            ],
          ),
          const SizedBox(height: 8),
          TweenAnimationBuilder<double>(
            tween: Tween(begin: 0, end: metrica.atual.toDouble()),
            duration: const Duration(milliseconds: 700),
            curve: Curves.easeOutCubic,
            builder: (context, valor, _) => Text(
              valor.round().toString(),
              style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w800, height: 1),
            ),
          ),
          const SizedBox(height: 2),
          Text(rotulo, style: const TextStyle(color: AppCores.textoSecundario, fontWeight: FontWeight.w600, fontSize: 12.5)),
        ],
      ),
    );
  }
}
