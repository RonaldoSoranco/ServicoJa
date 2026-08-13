package com.servicoja.infra.excecao;

public class CredenciaisInvalidasException extends NegocioException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
