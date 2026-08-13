package com.servicoja.pagamento.asaas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
public class AsaasPropriedades {

    private final String url;
    private final String apiKey;
    private final String webhookSegredo;
    private final BigDecimal valorMensal;
    private final BigDecimal valorAnual;

    public AsaasPropriedades(
            @Value("${servico-ja.asaas.url}") String url,
            @Value("${servico-ja.asaas.api-key}") String apiKey,
            @Value("${servico-ja.asaas.webhook-segredo}") String webhookSegredo,
            @Value("${servico-ja.asaas.valor-mensal}") BigDecimal valorMensal,
            @Value("${servico-ja.asaas.valor-anual}") BigDecimal valorAnual,
            @Value("${spring.profiles.active:}") String perfisAtivos) {
        if (ehProducao(perfisAtivos) && (webhookSegredo == null || webhookSegredo.isBlank())) {
            throw new IllegalStateException(
                    "O segredo do webhook do Asaas (ASAAS_WEBHOOK_SEGREDO) e obrigatorio em producao.");
        }
        this.url = url;
        this.apiKey = apiKey;
        this.webhookSegredo = webhookSegredo;
        this.valorMensal = valorMensal;
        this.valorAnual = valorAnual;
    }

    private boolean ehProducao(String perfisAtivos) {
        return Arrays.asList(perfisAtivos.split(",")).contains("prod");
    }

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getWebhookSegredo() {
        return webhookSegredo;
    }

    public BigDecimal getValorMensal() {
        return valorMensal;
    }

    public BigDecimal getValorAnual() {
        return valorAnual;
    }

    public boolean configurado() {
        return apiKey != null && !apiKey.isBlank();
    }
}
