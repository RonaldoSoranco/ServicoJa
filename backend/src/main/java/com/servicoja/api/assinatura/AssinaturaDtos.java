package com.servicoja.api.assinatura;

import com.servicoja.dominio.assinatura.StatusAssinatura;
import com.servicoja.dominio.assinatura.TipoAssinatura;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class AssinaturaDtos {

    private AssinaturaDtos() {
    }

    public record CriarAssinaturaRequest(
            @NotNull(message = "Informe a empresa.") Long empresaId,
            @NotNull(message = "Informe o tipo de assinatura.") TipoAssinatura tipo) {
    }

    public record AssinaturaResposta(
            Long id,
            Long empresaId,
            TipoAssinatura tipo,
            StatusAssinatura status,
            OffsetDateTime inicioEm,
            OffsetDateTime fimEm,
            BigDecimal valor,
            String linkPagamento,
            boolean premiumAtivo) {
    }

    public record MensagemResposta(String mensagem) {
    }
}
