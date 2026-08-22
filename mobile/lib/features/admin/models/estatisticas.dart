class Estatisticas {
  Estatisticas({
    required this.totalUsuarios,
    required this.totalClientes,
    required this.totalEmpresas,
    required this.totalEmpresasPremium,
    required this.empresasPendentes,
    required this.avaliacoesPendentes,
    required this.avaliacoesAprovadas,
    required this.totalAvaliacoes,
  });

  factory Estatisticas.fromJson(Map<String, dynamic> json) {
    return Estatisticas(
      totalUsuarios: (json['totalUsuarios'] as num).toInt(),
      totalClientes: (json['totalClientes'] as num).toInt(),
      totalEmpresas: (json['totalEmpresas'] as num).toInt(),
      totalEmpresasPremium: (json['totalEmpresasPremium'] as num).toInt(),
      empresasPendentes: (json['empresasPendentes'] as num).toInt(),
      avaliacoesPendentes: (json['avaliacoesPendentes'] as num).toInt(),
      avaliacoesAprovadas: (json['avaliacoesAprovadas'] as num).toInt(),
      totalAvaliacoes: (json['totalAvaliacoes'] as num).toInt(),
    );
  }

  final int totalUsuarios;
  final int totalClientes;
  final int totalEmpresas;
  final int totalEmpresasPremium;
  final int empresasPendentes;
  final int avaliacoesPendentes;
  final int avaliacoesAprovadas;
  final int totalAvaliacoes;
}
