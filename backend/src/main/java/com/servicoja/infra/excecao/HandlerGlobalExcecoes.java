package com.servicoja.infra.excecao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestControllerAdvice
public class HandlerGlobalExcecoes {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerGlobalExcecoes.class);

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResposta> credenciaisInvalidas(CredenciaisInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErroResposta.de(401, "CREDENCIAIS_INVALIDAS", ex.getMessage()));
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<ErroResposta> naoAutenticado(NaoAutenticadoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErroResposta.de(401, "NAO_AUTENTICADO", ex.getMessage()));
    }

    @ExceptionHandler(LimiteExcedidoException.class)
    public ResponseEntity<ErroResposta> limiteExcedido(LimiteExcedidoException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErroResposta.de(429, "LIMITE_DE_REQUISICOES", ex.getMessage()));
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErroResposta> negocio(NegocioException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErroResposta.de(422, "REGRA_DE_NEGOCIO", ex.getMessage()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroResposta.de(404, "NAO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResposta> acessoNegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErroResposta.de(403, "ACESSO_NEGADO", "Voce nao tem permissao para realizar esta acao."));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResposta> statusDeResposta(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return ResponseEntity.status(status)
                .body(ErroResposta.de(status.value(), "REQUISICAO_RECUSADA", ex.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(ErroResposta.de(400, "VALIDACAO", "Dados invalidos.", detalhes));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroResposta> requisicaoMalformada(Exception ex) {
        return ResponseEntity.badRequest()
                .body(ErroResposta.de(400, "REQUISICAO_INVALIDA", "Corpo ou parametros da requisicao invalidos."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> violacaoDeIntegridade(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResposta.de(409, "CONFLITO", "A operacao conflita com dados existentes."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> argumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ErroResposta.de(400, "ARGUMENTO_INVALIDO", "Parametro invalido."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> inesperada(Exception ex) {
        LOGGER.error("Erro inesperado na API", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErroResposta.de(500, "ERRO_INTERNO", "Ocorreu um erro inesperado. Tente novamente."));
    }
}
