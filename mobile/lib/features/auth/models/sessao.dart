import 'usuario.dart';

class Sessao {
  Sessao({required this.tokenAcesso, required this.tokenRefresh, required this.usuario});

  factory Sessao.fromJson(Map<String, dynamic> json) {
    return Sessao(
      tokenAcesso: json['tokenAcesso'] as String,
      tokenRefresh: json['tokenRefresh'] as String,
      usuario: Usuario.fromJson(json['usuario'] as Map<String, dynamic>),
    );
  }

  final String tokenAcesso;
  final String tokenRefresh;
  final Usuario usuario;
}
