import 'package:geolocator/geolocator.dart';
import 'package:latlong2/latlong.dart';

/// Centro de Marau (RS), usado quando ainda não há uma localização para mostrar no mapa.
const centroPadrao = LatLng(-28.4489, -52.1992);

/// Obtém a posição atual do aparelho, pedindo permissão quando necessário.
class LocalizacaoService {
  /// Retorna `null` se o GPS estiver desligado, a permissão for negada ou demorar demais.
  Future<LatLng?> posicaoAtual() async {
    try {
      if (!await Geolocator.isLocationServiceEnabled()) return null;
      var permissao = await Geolocator.checkPermission();
      if (permissao == LocationPermission.denied) {
        permissao = await Geolocator.requestPermission();
      }
      if (permissao == LocationPermission.denied || permissao == LocationPermission.deniedForever) {
        return null;
      }
      final posicao = await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(accuracy: LocationAccuracy.medium, timeLimit: Duration(seconds: 15)),
      );
      return LatLng(posicao.latitude, posicao.longitude);
    } catch (_) {
      return null;
    }
  }
}
