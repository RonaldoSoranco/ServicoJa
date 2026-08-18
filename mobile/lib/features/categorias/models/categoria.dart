class Categoria {
  Categoria({required this.id, required this.nome, this.descricao, this.icone});

  factory Categoria.fromJson(Map<String, dynamic> json) {
    return Categoria(
      id: (json['id'] as num).toInt(),
      nome: json['nome'] as String,
      descricao: json['descricao'] as String?,
      icone: json['icone'] as String?,
    );
  }

  final int id;
  final String nome;
  final String? descricao;
  final String? icone;
}
