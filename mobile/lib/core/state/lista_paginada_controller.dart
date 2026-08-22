import 'package:flutter/foundation.dart';

import '../network/api_exception.dart';
import '../network/pagina_resposta.dart';

/// Busca uma pagina de itens do tipo [T] no backend.
typedef BuscaPagina<T> = Future<PaginaResposta<T>> Function(int pagina, int tamanho);

/// Controller generico para listas paginadas com scroll infinito e pull-to-refresh.
///
/// Cobre o padrao repetido em varias telas: `itens`, `carregando`, `carregandoMais`,
/// `erro`, `temMais`, `carregarInicial()`, `carregarMais()`, `atualizar()` e
/// `reiniciar(novaBusca)` para quando os filtros de busca mudam.
class ListaPaginadaController<T> extends ChangeNotifier {
  ListaPaginadaController({required BuscaPagina<T> buscar, this.tamanhoPagina = 20}) : _buscar = buscar;

  BuscaPagina<T> _buscar;
  final int tamanhoPagina;

  List<T> itens = [];
  bool carregando = false;
  bool carregandoMais = false;
  String? erro;
  bool temMais = true;
  int _pagina = 0;

  Future<void> carregarInicial() async {
    carregando = true;
    erro = null;
    notifyListeners();
    try {
      final resposta = await _buscar(0, tamanhoPagina);
      itens = resposta.conteudo;
      temMais = resposta.temProximaPagina;
      _pagina = 0;
    } on ApiException catch (e) {
      erro = e.toString();
    } catch (_) {
      erro = 'Ocorreu um erro inesperado. Tente novamente.';
    } finally {
      carregando = false;
      notifyListeners();
    }
  }

  Future<void> carregarMais() async {
    if (carregandoMais || carregando || !temMais) return;
    carregandoMais = true;
    notifyListeners();
    try {
      final proximaPagina = _pagina + 1;
      final resposta = await _buscar(proximaPagina, tamanhoPagina);
      itens = [...itens, ...resposta.conteudo];
      _pagina = proximaPagina;
      temMais = resposta.temProximaPagina;
    } on ApiException catch (e) {
      erro = e.toString();
    } catch (_) {
      erro = 'Ocorreu um erro inesperado. Tente novamente.';
    } finally {
      carregandoMais = false;
      notifyListeners();
    }
  }

  Future<void> atualizar() => carregarInicial();

  /// Troca a funcao de busca (ex.: novos filtros) e recarrega do zero.
  Future<void> reiniciar(BuscaPagina<T> novaBusca) {
    _buscar = novaBusca;
    return carregarInicial();
  }

  /// Remove um item localmente sem recarregar a lista inteira (ex.: exclusao otimista).
  void removerLocal(bool Function(T) predicado) {
    itens = itens.where((e) => !predicado(e)).toList();
    notifyListeners();
  }

  void inserirLocal(int indice, T item) {
    final novaLista = [...itens];
    novaLista.insert(indice.clamp(0, novaLista.length), item);
    itens = novaLista;
    notifyListeners();
  }

  /// Substitui a lista inteira localmente (ex.: atualizar o estado de um item
  /// sem refazer a chamada de rede) e notifica os ouvintes.
  void substituirItens(List<T> novaLista) {
    itens = novaLista;
    notifyListeners();
  }
}
