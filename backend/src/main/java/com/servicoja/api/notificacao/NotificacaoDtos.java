package com.servicoja.api.notificacao;

import com.servicoja.dominio.notificacao.TipoNotificacao;

import java.time.OffsetDateTime;

public final class NotificacaoDtos {

    private NotificacaoDtos() {
    }

    public record NotificacaoResposta(
            Long id,
            String titulo,
            String mensagem,
            boolean lida,
            TipoNotificacao tipo,
            OffsetDateTime criadoEm) {
    }

    public record NaoLidasResposta(long quantidade) {
    }

    public record MensagemResposta(String mensagem) {
    }
}
