/// Monta os links usados pelos botões de contato do perfil da empresa.
class ContatoEmpresa {
  ContatoEmpresa._();

  /// Link do WhatsApp (wa.me) com uma mensagem inicial. Números brasileiros sem DDI
  /// (10 ou 11 dígitos com DDD) recebem o 55 na frente.
  static Uri? whatsapp(String? numero, {required String nomeEmpresa}) {
    final digitos = _digitosComDdi(numero);
    if (digitos == null) return null;
    final mensagem = 'Olá, $nomeEmpresa! Encontrei vocês no Serviço Já e gostaria de mais informações.';
    return Uri.https('wa.me', '/$digitos', {'text': mensagem});
  }

  static Uri? telefone(String? numero) {
    final digitos = numero?.replaceAll(RegExp(r'\D'), '') ?? '';
    if (digitos.length < 8) return null;
    return Uri(scheme: 'tel', path: digitos);
  }

  /// Rota até a empresa no app de mapas (Google Maps; no navegador, se o app não estiver instalado).
  static Uri comoChegar(double latitude, double longitude) {
    return Uri.https('www.google.com', '/maps/dir/', {'api': '1', 'destination': '$latitude,$longitude'});
  }

  static String? _digitosComDdi(String? numero) {
    final digitos = numero?.replaceAll(RegExp(r'\D'), '') ?? '';
    if (digitos.length == 10 || digitos.length == 11) return '55$digitos';
    if (digitos.startsWith('55') && (digitos.length == 12 || digitos.length == 13)) return digitos;
    return digitos.length >= 8 ? digitos : null;
  }
}
