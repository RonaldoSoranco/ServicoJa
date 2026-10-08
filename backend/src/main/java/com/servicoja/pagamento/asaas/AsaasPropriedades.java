package com.servicoja.pagamento.asaas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
public class AsaasPropriedades {

    /** Limites exigidos pelo Asaas para o token de autenticacao do webhook. */
    static final int TAMANHO_MINIMO_TOKEN_WEBHOOK = 32;
    static final int TAMANHO_MAXIMO_TOKEN_WEBHOOK = 255;

    private final String url;
    private final String apiKey;
    private final String webhookToken;
    private final BigDecimal valorMensal;
    private final BigDecimal valorAnual;

    public AsaasPropriedades(
            @Value("${servico-ja.asaas.url}") String url,
            @Value("${servico-ja.asaas.api-key}") String apiKey,
            @Value("${servico-ja.asaas.webhook-token}") String webhookToken,
            @Value("${servico-ja.asaas.valor-mensal}") BigDecimal valorMensal,
            @Value("${servico-ja.asaas.valor-anual}") BigDecimal valorAnual,
            @Value("${spring.profiles.active:}") String perfisAtivos) {
        this.url = url;
        this.apiKey = apiKey;
        this.webhookToken = webhookToken;
        this.valorMensal = valorMensal;
        this.valorAnual = valorAnual;
        if (ehProducao(perfisAtivos) && configurado()) {
            validarTokenWebhook();
        }
    }

    private void validarTokenWebhook() {
        if (webhookToken == null || webhookToken.isBlank()) {
            throw new IllegalStateException(
                    "O token do webhook do Asaas (ASAAS_WEBHOOK_TOKEN) e obrigatorio em producao.");
        }
        int tamanho = webhookToken.length();
        if (tamanho < TAMANHO_MINIMO_TOKEN_WEBHOOK || tamanho > TAMANHO_MAXIMO_TOKEN_WEBHOOK) {
            throw new IllegalStateException(
                    "O token do webhook do Asaas (ASAAS_WEBHOOK_TOKEN) deve ter entre "
                            + TAMANHO_MINIMO_TOKEN_WEBHOOK + " e " + TAMANHO_MAXIMO_TOKEN_WEBHOOK + " caracteres.");
        }
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

    public String getWebhookToken() {
        return webhookToken;
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
