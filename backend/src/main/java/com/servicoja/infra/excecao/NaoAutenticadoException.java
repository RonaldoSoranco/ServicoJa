package com.servicoja.infra.excecao;

public class NaoAutenticadoException extends RuntimeException {

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
