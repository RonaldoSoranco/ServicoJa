import 'package:flutter/foundation.dart';

import '../../../core/network/api_exception.dart';
import '../data/auth_repository.dart';
import '../models/usuario.dart';

enum StatusAuth { verificando, autenticado, naoAutenticado }

class AuthController extends ChangeNotifier {
  AuthController({AuthRepository? repositorio}) : _repositorio = repositorio ?? AuthRepository();

  final AuthRepository _repositorio;

  StatusAuth status = StatusAuth.verificando;
  Usuario? usuario;
  bool carregando = false;
  String? erro;

  Future<void> verificarSessao() async {
    Usuario? u;
    try {
      u = await _repositorio.sessaoAtual();
    } catch (_) {
      u = null;
    }
    usuario = u;
    status = u != null ? StatusAuth.autenticado : StatusAuth.naoAutenticado;
    notifyListeners();
  }

  Future<bool> login({required String email, required String senha}) {
    return _executar(() async {
      final sessao = await _repositorio.login(email: email, senha: senha);
      usuario = sessao.usuario;
      status = StatusAuth.autenticado;
    });
  }

  Future<bool> cadastrarCliente({
    required String nome,
    required String email,
    required String senha,
    String? telefone,
  }) {
    return _executar(() async {
      final sessao = await _repositorio.cadastrarCliente(nome: nome, email: email, senha: senha, telefone: telefone);
      usuario = sessao.usuario;
      status = StatusAuth.autenticado;
    });
  }

  Future<bool> cadastrarEmpresa({
    required String nomeResponsavel,
    required String email,
    required String senha,
    String? telefone,
    required String nomeEmpresa,
    required int categoriaId,
    required String cidade,
    required String uf,
    String? cpf,
  }) {
    return _executar(() async {
      final sessao = await _repositorio.cadastrarEmpresa(
        nomeResponsavel: nomeResponsavel,
        email: email,
        senha: senha,
        telefone: telefone,
        nomeEmpresa: nomeEmpresa,
        categoriaId: categoriaId,
        cidade: cidade,
        uf: uf,
        cpf: cpf,
      );
      usuario = sessao.usuario;
      status = StatusAuth.autenticado;
    });
  }

  Future<bool> atualizarPerfil({required String nome, String? telefone}) {
    return _executar(() async {
      usuario = await _repositorio.atualizarPerfil(nome: nome, telefone: telefone);
    });
  }

  Future<bool> alterarSenha({required String senhaAtual, required String novaSenha}) {
    return _executar(() async {
      await _repositorio.alterarSenha(senhaAtual: senhaAtual, novaSenha: novaSenha);
    });
  }

  Future<void> logout() async {
    carregando = true;
    notifyListeners();
    await _repositorio.logout();
    usuario = null;
    status = StatusAuth.naoAutenticado;
    carregando = false;
    notifyListeners();
  }

  Future<bool> _executar(Future<void> Function() acao) async {
    carregando = true;
    erro = null;
    notifyListeners();
    try {
      await acao();
      carregando = false;
      notifyListeners();
      return true;
    } on ApiException catch (e) {
      erro = e.toString();
      carregando = false;
      notifyListeners();
      return false;
    } catch (_) {
      erro = 'Ocorreu um erro inesperado. Tente novamente.';
      carregando = false;
      notifyListeners();
      return false;
    }
  }
}
