package com.servicoja.api.favorito;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class FavoritoDtos {

    private FavoritoDtos() {
    }

    public record FavoritoResposta(
            Long empresaId,
            String nomeEmpresa,
            String logoUrl,
            String cidade,
            String uf,
            boolean premiumAtivo,
            boolean destaque,
            BigDecimal mediaAvaliacoes,
            Integer totalAvaliacoes) {
    }

    public record EstadoResposta(@NotNull boolean favoritado) {
    }

    public record MensagemResposta(String mensagem) {
    }
}
