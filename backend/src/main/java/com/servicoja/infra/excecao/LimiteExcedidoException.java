package com.servicoja.infra.excecao;

public class LimiteExcedidoException extends NegocioException {

    public LimiteExcedidoException(String mensagem) {
        super(mensagem);
    }
}
