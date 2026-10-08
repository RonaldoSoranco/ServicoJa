import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../models/empresa.dart';
import 'secao_bloqueada_premium.dart';

/// Secao "Fotos" da tela de edicao de empresa — exclusiva para Premium.
class FotosSecao extends StatelessWidget {
  const FotosSecao({
    super.key,
    required this.premiumAtivo,
    required this.fotos,
    required this.aoAdicionar,
    required this.aoRemover,
  });

  final bool premiumAtivo;
  final List<Foto> fotos;
  final Future<void> Function(List<int> bytes, String nomeArquivo, String? descricao) aoAdicionar;
  final Future<void> Function(Foto foto) aoRemover;

  Future<void> _selecionarEAdicionar(BuildContext context) async {
    final arquivo = await ImagePicker().pickImage(source: ImageSource.gallery, imageQuality: 85);
    if (arquivo == null || !context.mounted) return;

    final descricaoController = TextEditingController();
    final confirmar = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Adicionar foto'),
        content: TextField(
          controller: descricaoController,
          decoration: const InputDecoration(labelText: 'Descricao (opcional)'),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Cancelar')),
          FilledButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('Adicionar')),
        ],
      ),
    );
    if (confirmar != true) return;

    final bytes = await arquivo.readAsBytes();
    await aoAdicionar(bytes, arquivo.name, descricaoController.text.trim());
  }

  @override
  Widget build(BuildContext context) {
    if (!premiumAtivo) {
      return const SecaoBloqueadaPremium(
        titulo: 'Fotos (Premium)',
        mensagem: 'O envio de fotos e exclusivo para empresas Premium. Assine para liberar esta secao.',
      );
    }
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            const Expanded(child: Text('Fotos', style: TextStyle(fontWeight: FontWeight.bold))),
            TextButton.icon(
              onPressed: () => _selecionarEAdicionar(context),
              icon: const Icon(Icons.add_a_photo_outlined),
              label: const Text('Adicionar'),
            ),
          ],
        ),
        if (fotos.isEmpty)
          const Padding(
            padding: EdgeInsets.symmetric(vertical: 8),
            child: Text('Nenhuma foto cadastrada.', style: TextStyle(color: Colors.black45)),
          )
        else
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: fotos.map((foto) => _fotoItem(context, foto)).toList(),
          ),
      ],
    );
  }

  Widget _fotoItem(BuildContext context, Foto foto) {
    return Stack(
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(10),
          child: Image.network(
            foto.url,
            width: 90,
            height: 90,
            fit: BoxFit.cover,
            errorBuilder: (_, _, _) => Container(
              width: 90,
              height: 90,
              color: Colors.grey.shade100,
              child: const Icon(Icons.broken_image_outlined, color: Colors.black26),
            ),
          ),
        ),
        Positioned(
          top: 2,
          right: 2,
          child: InkWell(
            onTap: () => aoRemover(foto),
            child: Container(
              padding: const EdgeInsets.all(2),
              decoration: const BoxDecoration(color: Colors.black54, shape: BoxShape.circle),
              child: const Icon(Icons.close, size: 16, color: Colors.white),
            ),
          ),
        ),
      ],
    );
  }
}
