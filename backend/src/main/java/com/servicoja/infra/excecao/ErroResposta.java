package com.servicoja.infra.excecao;

import java.time.OffsetDateTime;
import java.util.List;

public record ErroResposta(
        OffsetDateTime momento,
        int codigo,
        String status,
        String mensagem,
        List<String> detalhes
) {

    public static ErroResposta de(int codigo, String status, String mensagem, List<String> detalhes) {
        return new ErroResposta(OffsetDateTime.now(), codigo, status, mensagem, detalhes);
    }

    public static ErroResposta de(int codigo, String status, String mensagem) {
        return de(codigo, status, mensagem, List.of());
    }
}
