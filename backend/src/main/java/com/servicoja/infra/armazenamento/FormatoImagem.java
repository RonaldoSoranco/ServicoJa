package com.servicoja.infra.armazenamento;

import java.util.Arrays;
import java.util.Optional;

/**
 * Formatos de imagem aceitos no upload. O formato e identificado pelos primeiros bytes do
 * arquivo (assinatura binaria), nunca pelo nome ou pelo Content-Type enviados pelo cliente,
 * que podem ser forjados para disfarcar outro tipo de arquivo (ex.: HTML ou SVG com script).
 */
enum FormatoImagem {

    JPEG("jpg"),
    PNG("png"),
    WEBP("webp");

    /** Quantidade de bytes iniciais suficiente para reconhecer qualquer um dos formatos. */
    static final int TAMANHO_CABECALHO = 12;

    private static final byte[] ASSINATURA_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] ASSINATURA_PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] ASSINATURA_RIFF = {'R', 'I', 'F', 'F'};
    private static final byte[] ASSINATURA_WEBP = {'W', 'E', 'B', 'P'};

    private final String extensao;

    FormatoImagem(String extensao) {
        this.extensao = extensao;
    }

    String extensao() {
        return extensao;
    }

    static Optional<FormatoImagem> detectar(byte[] cabecalho) {
        if (contemNaPosicao(cabecalho, 0, ASSINATURA_JPEG)) {
            return Optional.of(JPEG);
        }
        if (contemNaPosicao(cabecalho, 0, ASSINATURA_PNG)) {
            return Optional.of(PNG);
        }
        if (contemNaPosicao(cabecalho, 0, ASSINATURA_RIFF) && contemNaPosicao(cabecalho, 8, ASSINATURA_WEBP)) {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    private static boolean contemNaPosicao(byte[] dados, int posicao, byte[] assinatura) {
        int fim = posicao + assinatura.length;
        return dados.length >= fim && Arrays.equals(dados, posicao, fim, assinatura, 0, assinatura.length);
    }
}
