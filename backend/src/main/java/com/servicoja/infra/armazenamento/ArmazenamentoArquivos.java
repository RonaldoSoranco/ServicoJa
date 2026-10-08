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
     * Valida que o arquivo e uma imagem aceita, salva-o e retorna o caminho publico relativo
     * (ex: "/uploads/empresas/1/fotos/uuid.jpg").
     */
    String salvar(MultipartFile arquivo, String subpasta);

    /**
     * Remove o arquivo referenciado pelo caminho publico retornado por {@link #salvar}, desde que
     * ele esteja dentro de {@code subpasta}. Caminhos fora dela (URLs externas, arquivos de outra
     * empresa, tentativas com "..") sao ignorados.
     */
    void remover(String caminhoPublico, String subpasta);

    /**
     * Remove a subpasta e todo o seu conteudo (ex.: todos os arquivos de uma empresa excluida).
     */
    void removerPasta(String subpasta);
}
