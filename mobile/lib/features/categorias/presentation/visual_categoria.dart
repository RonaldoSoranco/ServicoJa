import 'package:flutter/material.dart';

import '../../../core/theme/app_theme.dart';

/// Ícone e cor de cada categoria, a partir do campo `icone` cadastrado no backend.
class VisualCategoria {
  const VisualCategoria(this.icone, this.cor);

  final IconData icone;
  final Color cor;

  static VisualCategoria de(String? chave) {
    return switch (chave) {
      'eletricista' => const VisualCategoria(Icons.bolt_rounded, Color(0xFFF59E0B)),
      'encanador' => const VisualCategoria(Icons.plumbing_rounded, Color(0xFF0EA5E9)),
      'beleza' => const VisualCategoria(Icons.content_cut_rounded, Color(0xFFEC4899)),
      'mecanica' => const VisualCategoria(Icons.car_repair_rounded, Color(0xFF64748B)),
      'limpeza' => const VisualCategoria(Icons.cleaning_services_rounded, Color(0xFF14B8A6)),
      'reparos' => const VisualCategoria(Icons.handyman_rounded, AppCores.laranja),
      'costura' => const VisualCategoria(Icons.checkroom_rounded, Color(0xFFA855F7)),
      'design' => const VisualCategoria(Icons.palette_rounded, Color(0xFF6366F1)),
      'educacao' => const VisualCategoria(Icons.school_rounded, Color(0xFF22C55E)),
      'pintura' => const VisualCategoria(Icons.format_paint_rounded, Color(0xFFEF4444)),
      _ => const VisualCategoria(Icons.storefront_rounded, AppCores.laranja),
    };
  }
}

/// Quadrado colorido com o ícone da categoria (usado quando a empresa ainda não tem logo).
class IconeCategoria extends StatelessWidget {
  const IconeCategoria({super.key, required this.chave, this.tamanho = 56});

  final String? chave;
  final double tamanho;

  @override
  Widget build(BuildContext context) {
    final visual = VisualCategoria.de(chave);
    return Container(
      width: tamanho,
      height: tamanho,
      decoration: BoxDecoration(
        color: visual.cor.withValues(alpha: 0.14),
        borderRadius: BorderRadius.circular(tamanho * 0.28),
      ),
      child: Icon(visual.icone, color: visual.cor, size: tamanho * 0.5),
    );
  }
}

/// Logo da empresa ou, sem logo, o ícone da categoria.
class LogoEmpresa extends StatelessWidget {
  const LogoEmpresa({super.key, required this.url, required this.chaveCategoria, this.tamanho = 56});

  final String? url;
  final String? chaveCategoria;
  final double tamanho;

  @override
  Widget build(BuildContext context) {
    final semLogo = IconeCategoria(chave: chaveCategoria, tamanho: tamanho);
    if (url == null || url!.isEmpty) return semLogo;
    return ClipRRect(
      borderRadius: BorderRadius.circular(tamanho * 0.28),
      child: Image.network(url!, width: tamanho, height: tamanho, fit: BoxFit.cover, errorBuilder: (_, _, _) => semLogo),
    );
  }
}
