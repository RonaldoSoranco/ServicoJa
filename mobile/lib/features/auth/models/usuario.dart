enum Perfil { cliente, empresa, admin }

Perfil perfilDeTexto(String valor) {
  switch (valor) {
    case 'CLIENTE':
      return Perfil.cliente;
    case 'EMPRESA':
      return Perfil.empresa;
    case 'ADMIN':
      return Perfil.admin;
    default:
      throw ArgumentError('Perfil desconhecido: $valor');
  }
}

class Usuario {
  Usuario({required this.id, required this.nome, required this.email, this.telefone, required this.perfil});

  factory Usuario.fromJson(Map<String, dynamic> json) {
    return Usuario(
      id: (json['id'] as num).toInt(),
      nome: json['nome'] as String,
      email: json['email'] as String,
      telefone: json['telefone'] as String?,
      perfil: perfilDeTexto(json['perfil'] as String),
    );
  }

  final int id;
  final String nome;
  final String email;
  final String? telefone;
  final Perfil perfil;
}
