package com.servicoja.infra.excecao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class HandlerGlobalExcecoes {

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(ErroResposta.de(400, "VALIDACAO", "Dados invalidos.", detalhes));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> argumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ErroResposta.de(400, "ARGUMENTO_INVALIDO", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> inesperada(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErroResposta.de(500, "ERRO_INTERNO", "Ocorreu um erro inesperado. Tente novamente."));
    }
}
