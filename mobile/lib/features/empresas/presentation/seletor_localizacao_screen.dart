import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import '../../../core/localizacao/localizacao_service.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/mapa.dart';

/// Tela para marcar a localização da empresa: o pino fica fixo no centro e a pessoa arrasta o
/// mapa até ele ficar sobre a empresa. Devolve o [LatLng] escolhido ao confirmar.
class SeletorLocalizacaoScreen extends StatefulWidget {
  const SeletorLocalizacaoScreen({super.key, this.inicial});

  final LatLng? inicial;

  @override
  State<SeletorLocalizacaoScreen> createState() => _SeletorLocalizacaoScreenState();
}

class _SeletorLocalizacaoScreenState extends State<SeletorLocalizacaoScreen> {
  final _mapa = MapController();
  final _localizacao = LocalizacaoService();
  late LatLng _centro = widget.inicial ?? centroPadrao;
  bool _buscandoPosicao = false;

  @override
  void initState() {
    super.initState();
    if (widget.inicial == null) {
      WidgetsBinding.instance.addPostFrameCallback((_) => _irParaMinhaLocalizacao(avisarSeFalhar: false));
    }
  }

  @override
  void dispose() {
    _mapa.dispose();
    super.dispose();
  }

  Future<void> _irParaMinhaLocalizacao({bool avisarSeFalhar = true}) async {
    setState(() => _buscandoPosicao = true);
    final posicao = await _localizacao.posicaoAtual();
    if (!mounted) return;
    setState(() => _buscandoPosicao = false);
    if (posicao == null) {
      if (avisarSeFalhar) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
          content: Text('Não conseguimos sua localização. Arraste o mapa até o endereço da empresa.'),
        ));
      }
      return;
    }
    _mapa.move(posicao, 17);
    setState(() => _centro = posicao);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Localização da empresa')),
      body: Stack(
        children: [
          FlutterMap(
            mapController: _mapa,
            options: MapOptions(
              initialCenter: _centro,
              initialZoom: widget.inicial != null ? 17 : 14,
              interactionOptions: const InteractionOptions(flags: InteractiveFlag.all & ~InteractiveFlag.rotate),
              onPositionChanged: (camera, _) => _centro = camera.center,
            ),
            children: [camadaMapa(), creditoMapa()],
          ),
          // O pino desenha a ponta no centro exato do mapa.
          const IgnorePointer(
            child: Center(
              child: Padding(
                padding: EdgeInsets.only(bottom: PinoEmpresa.altura),
                child: PinoEmpresa(selecionado: true),
              ),
            ),
          ),
          const Positioned(
            top: 12,
            left: 16,
            right: 16,
            child: Card(
              child: Padding(
                padding: EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                child: Row(
                  children: [
                    Icon(Icons.pan_tool_alt_outlined, color: AppCores.laranja),
                    SizedBox(width: 10),
                    Expanded(child: Text('Arraste o mapa até o pino ficar sobre a sua empresa.')),
                  ],
                ),
              ),
            ),
          ),
          Positioned(
            right: 16,
            bottom: 96,
            child: FloatingActionButton.small(
              heroTag: 'minha-localizacao',
              tooltip: 'Usar minha localização',
              backgroundColor: Colors.white,
              foregroundColor: AppCores.laranja,
              onPressed: _buscandoPosicao ? null : _irParaMinhaLocalizacao,
              child: _buscandoPosicao
                  ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
                  : const Icon(Icons.my_location_rounded),
            ),
          ),
          Positioned(
            left: 16,
            right: 16,
            bottom: 24,
            child: SafeArea(
              child: FilledButton.icon(
                style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(54)),
                onPressed: () => Navigator.of(context).pop(_centro),
                icon: const Icon(Icons.check_rounded),
                label: const Text('Confirmar localização'),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
