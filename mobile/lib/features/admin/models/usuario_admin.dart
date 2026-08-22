import '../../auth/models/usuario.dart';

class UsuarioAdmin {
  UsuarioAdmin({
    required this.id,
    required this.nome,
    required this.email,
    required this.perfil,
    required this.ativo,
    required this.criadoEm,
  });

  factory UsuarioAdmin.fromJson(Map<String, dynamic> json) {
    return UsuarioAdmin(
      id: (json['id'] as num).toInt(),
      nome: json['nome'] as String,
      email: json['email'] as String,
      perfil: perfilDeTexto(json['perfil'] as String),
      ativo: json['ativo'] as bool? ?? true,
      criadoEm: DateTime.parse(json['criadoEm'] as String),
    );
  }

  final int id;
  final String nome;
  final String email;
  final Perfil perfil;
  final bool ativo;
  final DateTime criadoEm;
}
