package com.servicoja.infra.excecao;

public class NegocioException extends RuntimeException {

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
