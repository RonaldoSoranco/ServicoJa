package com.servicoja.pagamento.asaas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
            @Value("${servico-ja.asaas.valor-anual}") BigDecimal valorAnual) {
        this.url = url;
        this.apiKey = apiKey;
        this.webhookSegredo = webhookSegredo;
        this.valorMensal = valorMensal;
        this.valorAnual = valorAnual;
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
