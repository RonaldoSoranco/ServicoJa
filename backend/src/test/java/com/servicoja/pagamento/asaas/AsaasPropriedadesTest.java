package com.servicoja.pagamento.asaas;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AsaasPropriedadesTest {

    private static final String CHAVE_API = "$aact_chave_de_teste";
    private static final String TOKEN_VALIDO = "a".repeat(AsaasPropriedades.TAMANHO_MINIMO_TOKEN_WEBHOOK);

    @Test
    void producaoComPagamentosAtivosExigeTokenDoWebhook() {
        assertThatThrownBy(() -> criar(CHAVE_API, "", "prod"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ASAAS_WEBHOOK_TOKEN");
    }

    @Test
    void producaoRecusaTokenMaisCurtoQueOExigidoPeloAsaas() {
        assertThatThrownBy(() -> criar(CHAVE_API, TOKEN_VALIDO.substring(1), "prod"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entre 32 e 255");
    }

    @Test
    void producaoComTokenValidoSobeNormalmente() {
        AsaasPropriedades propriedades = criar(CHAVE_API, TOKEN_VALIDO, "prod");

        assertThat(propriedades.getWebhookToken()).isEqualTo(TOKEN_VALIDO);
    }

    @Test
    void producaoSemPagamentosConfiguradosNaoExigeToken() {
        assertThatNoException().isThrownBy(() -> criar("", "", "prod"));
    }

    @Test
    void foraDeProducaoNaoValidaToken() {
        assertThatNoException().isThrownBy(() -> criar(CHAVE_API, "", ""));
    }

    private AsaasPropriedades criar(String chaveApi, String token, String perfis) {
        return new AsaasPropriedades(
                "https://api.asaas.com", chaveApi, token, BigDecimal.TEN, BigDecimal.TEN, perfis);
    }
}
