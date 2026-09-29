package com.servicoja.pagamento.asaas;

import com.servicoja.api.assinatura.AssinaturaService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Testa a verificacao de assinatura HMAC do webhook do Asaas sem subir o contexto Spring
 * (evita depender da API externa do Asaas para validar apenas a regra de seguranca do endpoint).
 */
class AsaasWebhookControllerTest {

    private static final String SEGREDO = "segredo-teste-webhook";

    private final AssinaturaService assinaturaService = mock(AssinaturaService.class);
    private final AsaasPropriedades propriedades = new AsaasPropriedades(
            "https://sandbox.asaas.com", "", SEGREDO, BigDecimal.TEN, BigDecimal.TEN, "");
    private final AsaasWebhookController controller = new AsaasWebhookController(assinaturaService, propriedades);

    @Test
    void aceitaEProcessaQuandoAssinaturaEValida() {
        String corpo = "{\"event\":\"PAYMENT_CONFIRMED\"}";
        String assinatura = "sha256=" + calcularHmac(SEGREDO, corpo);

        var resposta = controller.webhook(corpo, assinatura);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        verify(assinaturaService).processarWebhook(corpo);
    }

    @Test
    void rejeitaQuandoAssinaturaEstaAusente() {
        ResponseStatusException ex =
                catchThrowableOfType(() -> controller.webhook("{}", null), ResponseStatusException.class);

        assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void rejeitaQuandoAssinaturaEstaIncorreta() {
        ResponseStatusException ex = catchThrowableOfType(
                () -> controller.webhook("{}", "sha256=" + "0".repeat(64)), ResponseStatusException.class);

        assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void rejeitaQuandoCorpoFoiAlteradoAposAAssinatura() {
        String assinatura = "sha256=" + calcularHmac(SEGREDO, "{\"event\":\"PAYMENT_CONFIRMED\"}");

        ResponseStatusException ex = catchThrowableOfType(
                () -> controller.webhook("{\"event\":\"PAYMENT_REFUNDED\"}", assinatura),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    private String calcularHmac(String segredo, String corpo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(corpo.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
