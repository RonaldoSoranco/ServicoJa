import 'package:flutter/material.dart';

import '../../../../core/theme/app_theme.dart';
import '../../../../core/widgets/selo.dart';
import '../../../categorias/presentation/visual_categoria.dart';
import '../../models/empresa_simples.dart';

String tagLogoEmpresa(int empresaId) => 'logo-empresa-$empresaId';

/// Nota média com estrela, ou "Novo" quando a empresa ainda não tem avaliações.
class NotaEmpresa extends StatelessWidget {
  const NotaEmpresa({super.key, required this.media, required this.total, this.tamanho = 13});

  final double? media;
  final int? total;
  final double tamanho;

  @override
  Widget build(BuildContext context) {
    if (media == null || (total ?? 0) == 0) {
      return const Selo(texto: 'Novo', cor: AppCores.laranjaEscuro, icone: Icons.auto_awesome_rounded);
    }
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(Icons.star_rounded, size: tamanho + 4, color: AppCores.estrela),
        const SizedBox(width: 2),
        Text(media!.toStringAsFixed(1).replaceAll('.', ','),
            style: TextStyle(fontWeight: FontWeight.w800, fontSize: tamanho)),
        Text(' ($total)', style: TextStyle(color: AppCores.textoSecundario, fontSize: tamanho - 1)),
      ],
    );
  }
}

/// Item da busca: logo, nome, categoria, nota, distância e se está aberto agora.
class CartaoEmpresa extends StatelessWidget {
  const CartaoEmpresa({super.key, required this.empresa, required this.aoTocar});

  final EmpresaSimples empresa;
  final VoidCallback aoTocar;

  @override
  Widget build(BuildContext context) {
    final visual = VisualCategoria.de(empresa.categoria.icone);
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      clipBehavior: Clip.antiAlias,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(AppTheme.raio),
        side: BorderSide(color: empresa.destaque ? AppCores.laranja.withValues(alpha: 0.45) : AppCores.borda),
      ),
      child: InkWell(
        onTap: aoTocar,
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Hero(
                tag: tagLogoEmpresa(empresa.id),
                child: LogoEmpresa(url: empresa.logoUrl, chaveCategoria: empresa.categoria.icone, tamanho: 64),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            empresa.nome,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 16),
                          ),
                        ),
                        if (empresa.destaque)
                          const Padding(
                            padding: EdgeInsets.only(left: 6),
                            child: Icon(Icons.workspace_premium_rounded, color: AppCores.laranja, size: 20),
                          ),
                      ],
                    ),
                    const SizedBox(height: 2),
                    Row(
                      children: [
                        Icon(visual.icone, size: 14, color: visual.cor),
                        const SizedBox(width: 4),
                        Flexible(
                          child: Text(
                            empresa.categoria.nome,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(color: AppCores.textoSecundario, fontSize: 13, fontWeight: FontWeight.w600),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Wrap(
                      spacing: 6,
                      runSpacing: 6,
                      crossAxisAlignment: WrapCrossAlignment.center,
                      children: [
                        NotaEmpresa(media: empresa.mediaAvaliacoes, total: empresa.totalAvaliacoes),
                        if (empresa.distanciaKm != null)
                          Selo(
                            texto: formatarDistancia(empresa.distanciaKm!),
                            cor: AppCores.marinho,
                            icone: Icons.near_me_rounded,
                          ),
                        SeloFuncionamento(aberto: empresa.abertoAgora, temHorarios: empresa.temHorarios),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Cartão largo do carrossel de empresas em destaque (Premium).
class CartaoDestaque extends StatelessWidget {
  const CartaoDestaque({super.key, required this.empresa, required this.aoTocar});

  final EmpresaSimples empresa;
  final VoidCallback aoTocar;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 250,
      child: Material(
        borderRadius: BorderRadius.circular(AppTheme.raio + 4),
        clipBehavior: Clip.antiAlias,
        child: Ink(
          decoration: const BoxDecoration(gradient: AppCores.gradienteMarca),
          child: InkWell(
            onTap: aoTocar,
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(2),
                        decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(16)),
                        child: LogoEmpresa(url: empresa.logoUrl, chaveCategoria: empresa.categoria.icone, tamanho: 44),
                      ),
                      const Spacer(),
                      const Selo(
                        texto: 'Destaque',
                        cor: AppCores.laranjaEscuro,
                        fundo: Colors.white,
                        icone: Icons.workspace_premium_rounded,
                      ),
                    ],
                  ),
                  const Spacer(),
                  Text(
                    empresa.nome,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w800, fontSize: 17),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    [
                      empresa.categoria.nome,
                      if (empresa.distanciaKm != null) formatarDistancia(empresa.distanciaKm!),
                      if (empresa.temHorarios) empresa.abertoAgora ? 'Aberto agora' : 'Fechado',
                    ].join(' · '),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(color: Colors.white.withValues(alpha: 0.9), fontSize: 12.5, fontWeight: FontWeight.w600),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
