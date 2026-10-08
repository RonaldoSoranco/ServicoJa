package com.servicoja.pagamento.asaas;

import com.servicoja.api.assinatura.AssinaturaService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Testa a autenticacao do webhook do Asaas (token fixo no cabecalho asaas-access-token) sem subir
 * o contexto Spring, para validar apenas a regra de seguranca do endpoint.
 */
class AsaasWebhookControllerTest {

    private static final String TOKEN = "token-de-teste-do-webhook-com-mais-de-32-caracteres";
    private static final String CORPO = "{\"event\":\"PAYMENT_CONFIRMED\"}";

    private final AssinaturaService assinaturaService = mock(AssinaturaService.class);

    @Test
    void aceitaEProcessaQuandoTokenConfere() {
        var resposta = controllerComToken(TOKEN).webhook(CORPO, TOKEN);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        verify(assinaturaService).processarWebhook(CORPO);
    }

    @Test
    void rejeitaQuandoTokenEstaAusente() {
        ResponseStatusException ex = catchThrowableOfType(ResponseStatusException.class,
                () -> controllerComToken(TOKEN).webhook(CORPO, null));

        assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        verify(assinaturaService, never()).processarWebhook(anyString());
    }

    @Test
    void rejeitaQuandoTokenEstaIncorreto() {
        ResponseStatusException ex = catchThrowableOfType(ResponseStatusException.class,
                () -> controllerComToken(TOKEN).webhook(CORPO, TOKEN + "x"));

        assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        verify(assinaturaService, never()).processarWebhook(anyString());
    }

    @Test
    void recusaTudoQuandoTokenNaoEstaConfigurado() {
        ResponseStatusException ex = catchThrowableOfType(ResponseStatusException.class,
                () -> controllerComToken("").webhook(CORPO, ""));

        assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(assinaturaService, never()).processarWebhook(anyString());
    }

    private AsaasWebhookController controllerComToken(String tokenConfigurado) {
        AsaasPropriedades propriedades = new AsaasPropriedades(
                "https://api-sandbox.asaas.com", "", tokenConfigurado, BigDecimal.TEN, BigDecimal.TEN, "");
        return new AsaasWebhookController(assinaturaService, propriedades);
    }
}
