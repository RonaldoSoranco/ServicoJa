package com.servicoja.infra;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResposta<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas
) {

    public static <T> PageResposta<T> de(Page<T> paginaDados) {
        return new PageResposta<>(
                paginaDados.getContent(),
                paginaDados.getNumber(),
                paginaDados.getSize(),
                paginaDados.getTotalElements(),
                paginaDados.getTotalPages());
    }
}
