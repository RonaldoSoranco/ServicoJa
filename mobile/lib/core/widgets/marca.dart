import 'package:flutter/material.dart';

import '../theme/app_theme.dart';

/// Símbolo do Serviço Já: um pino de localização com um raio, sobre um quadrado laranja.
/// O pino representa "perto de você"; o raio, a rapidez do "Já".
class LogoServicoJa extends StatelessWidget {
  const LogoServicoJa({super.key, this.tamanho = 56, this.invertido = false});

  final double tamanho;

  /// Fundo branco com o pino laranja, para usar sobre fundos laranja.
  final bool invertido;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: tamanho,
      height: tamanho,
      decoration: BoxDecoration(
        gradient: invertido ? null : AppCores.gradienteMarca,
        color: invertido ? Colors.white : null,
        borderRadius: BorderRadius.circular(tamanho * 0.3),
        boxShadow: [
          BoxShadow(
            color: (invertido ? Colors.black : AppCores.laranja).withValues(alpha: invertido ? 0.18 : 0.35),
            blurRadius: tamanho * 0.25,
            offset: Offset(0, tamanho * 0.08),
          ),
        ],
      ),
      child: CustomPaint(
        painter: _PinoComRaio(
          corPino: invertido ? AppCores.laranja : Colors.white,
          corRaio: invertido ? Colors.white : AppCores.laranja,
        ),
      ),
    );
  }
}

class _PinoComRaio extends CustomPainter {
  const _PinoComRaio({required this.corPino, required this.corRaio});

  final Color corPino;
  final Color corRaio;

  @override
  void paint(Canvas canvas, Size size) {
    final s = size.width;
    final cx = s / 2;
    final cy = s * 0.42;
    final r = s * 0.25;
    final ponta = s * 0.82;

    final pino = Path()
      ..moveTo(cx, ponta)
      ..cubicTo(cx - r * 0.3, ponta - (ponta - cy) * 0.42, cx - r, cy + r * 0.62, cx - r, cy)
      ..arcToPoint(Offset(cx + r, cy), radius: Radius.circular(r))
      ..cubicTo(cx + r, cy + r * 0.62, cx + r * 0.3, ponta - (ponta - cy) * 0.42, cx, ponta)
      ..close();
    canvas.drawPath(pino, Paint()..color = corPino);

    final raio = Path()
      ..moveTo(cx + r * 0.18, cy - r * 0.72)
      ..lineTo(cx - r * 0.42, cy + r * 0.10)
      ..lineTo(cx - r * 0.02, cy + r * 0.10)
      ..lineTo(cx - r * 0.20, cy + r * 0.72)
      ..lineTo(cx + r * 0.44, cy - r * 0.14)
      ..lineTo(cx + r * 0.04, cy - r * 0.14)
      ..close();
    canvas.drawPath(raio, Paint()..color = corRaio);
  }

  @override
  bool shouldRepaint(covariant _PinoComRaio anterior) => anterior.corPino != corPino || anterior.corRaio != corRaio;
}

/// Logo + nome "Serviço Já", com o "Já" em laranja.
class MarcaServicoJa extends StatelessWidget {
  const MarcaServicoJa({super.key, this.tamanhoLogo = 64, this.vertical = true, this.corNome = AppCores.marinho});

  final double tamanhoLogo;
  final bool vertical;
  final Color corNome;

  @override
  Widget build(BuildContext context) {
    final nome = RichText(
      text: TextSpan(
        style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.w800,
              color: corNome,
              fontSize: tamanhoLogo * 0.42,
              letterSpacing: -0.5,
            ),
        children: const [
          TextSpan(text: 'Serviço '),
          TextSpan(text: 'Já', style: TextStyle(color: AppCores.laranja)),
        ],
      ),
    );
    final logo = LogoServicoJa(tamanho: tamanhoLogo);
    if (vertical) {
      return Column(mainAxisSize: MainAxisSize.min, children: [logo, SizedBox(height: tamanhoLogo * 0.25), nome]);
    }
    return Row(mainAxisSize: MainAxisSize.min, children: [logo, SizedBox(width: tamanhoLogo * 0.25), nome]);
  }
}
