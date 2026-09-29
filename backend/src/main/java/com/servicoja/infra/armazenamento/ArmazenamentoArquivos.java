package com.servicoja.infra.armazenamento;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstrai onde os arquivos enviados pelos usuarios (fotos, logo) sao gravados.
 * A implementacao padrao grava em disco local; uma futura migracao para um
 * bucket (S3, Cloudinary, etc.) so precisa de uma nova implementacao desta
 * interface, sem alterar controllers ou services.
 */
public interface ArmazenamentoArquivos {

    /**
     * Salva o arquivo e retorna o caminho publico relativo (ex: "/uploads/empresas/1/fotos/uuid.jpg").
     */
    String salvar(MultipartFile arquivo, String subpasta);

    /**
     * Remove o arquivo referenciado pelo caminho publico relativo retornado por {@link #salvar}.
     * Deve ser tolerante a caminhos que nao correspondem a um arquivo local (ex: URLs externas).
     */
    void remover(String caminhoRelativo);
}
