package com.servicoja.pagamento.asaas;

import java.math.BigDecimal;

public final class AsaasDtos {

    private AsaasDtos() {
    }

    public record CriarClienteRequest(
            String name,
            String email,
            String cpfCnpj) {
    }

    public record ClienteResponse(
            String id,
            String name,
            String email,
            String cpfCnpj) {
    }

    public record CriarAssinaturaRequest(
            String customer,
            String billingType,
            BigDecimal value,
            String nextDueDate,
            String cycle,
            String description) {
    }

    public record AssinaturaResponse(
            String id,
            String customer,
            String status,
            BigDecimal value,
            String cycle) {
    }

    public record CriarPagamentoRequest(
            String customer,
            String billingType,
            BigDecimal value,
            String dueDate,
            String subscription,
            String description) {
    }

    public record PagamentoResponse(
            String id,
            String status,
            String billingType,
            String invoiceUrl,
            String paymentLink,
            String pixQrCodeUrl) {
    }

    public record WebhookPayload(
            String event,
            PaymentWebhook payment,
            SubscriptionWebhook subscription) {
    }

    public record PaymentWebhook(
            String id,
            String subscription,
            String status,
            String billingType) {
    }

    public record SubscriptionWebhook(
            String id,
            String status) {
    }
}
