import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:url_launcher/url_launcher.dart';

import '../theme/app_theme.dart';

/// Peças comuns dos mapas do app (OpenStreetMap, sem chave de API).

/// Camada de mapa do OpenStreetMap. O identificador do app vai no User-Agent, como pede a
/// política de uso dos servidores do OSM.
TileLayer camadaMapa() {
  return TileLayer(
    urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
    userAgentPackageName: 'br.com.servicoja.servicoja_app',
    maxZoom: 19,
  );
}

/// Crédito obrigatório do OpenStreetMap; deve ser a última camada do mapa.
Widget creditoMapa() {
  return SimpleAttributionWidget(
    source: const Text('OpenStreetMap', style: TextStyle(fontSize: 11)),
    onTap: () => launchUrl(Uri.parse('https://www.openstreetmap.org/copyright')),
  );
}

/// Pino laranja que marca uma empresa no mapa.
class PinoEmpresa extends StatelessWidget {
  const PinoEmpresa({super.key, this.selecionado = false});

  final bool selecionado;

  static const largura = 48.0;
  static const altura = 48.0;

  @override
  Widget build(BuildContext context) {
    return AnimatedScale(
      scale: selecionado ? 1.25 : 1,
      duration: const Duration(milliseconds: 180),
      alignment: Alignment.bottomCenter,
      child: Icon(
        Icons.location_on_rounded,
        size: altura,
        color: selecionado ? AppCores.laranjaEscuro : AppCores.laranja,
        shadows: const [Shadow(color: Colors.black26, blurRadius: 6, offset: Offset(0, 2))],
      ),
    );
  }
}

/// Marcador azul com a posição de quem está usando o app.
class MarcadorUsuario extends StatelessWidget {
  const MarcadorUsuario({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFF2563EB),
        shape: BoxShape.circle,
        border: Border.all(color: Colors.white, width: 3),
        boxShadow: const [BoxShadow(color: Colors.black26, blurRadius: 6)],
      ),
    );
  }
}

Marker marcadorEmpresa(LatLng ponto, {bool selecionado = false, VoidCallback? aoTocar}) {
  return Marker(
    point: ponto,
    width: PinoEmpresa.largura,
    height: PinoEmpresa.altura,
    alignment: Alignment.topCenter,
    child: GestureDetector(onTap: aoTocar, child: PinoEmpresa(selecionado: selecionado)),
  );
}

/// Mapa pequeno e estático com o pino de uma localização (prévia no perfil e no formulário).
class MapaPrevia extends StatelessWidget {
  const MapaPrevia({super.key, required this.ponto, this.altura = 160, this.aoTocar});

  final LatLng ponto;
  final double altura;
  final VoidCallback? aoTocar;

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(AppTheme.raio),
      child: SizedBox(
        height: altura,
        child: Stack(
          children: [
            FlutterMap(
              key: ValueKey(ponto),
              options: MapOptions(
                initialCenter: ponto,
                initialZoom: 16,
                interactionOptions: const InteractionOptions(flags: InteractiveFlag.none),
              ),
              children: [
                camadaMapa(),
                MarkerLayer(markers: [marcadorEmpresa(ponto)]),
                creditoMapa(),
              ],
            ),
            if (aoTocar != null) Positioned.fill(child: Material(color: Colors.transparent, child: InkWell(onTap: aoTocar))),
          ],
        ),
      ),
    );
  }
}
