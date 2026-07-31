package com.servicoja.pagamento.asaas;

import com.servicoja.infra.excecao.NegocioException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AsaasCliente {

    private final AsaasPropriedades propriedades;
    private final RestClient restClient;

    public AsaasCliente(AsaasPropriedades propriedades) {
        this.propriedades = propriedades;
        this.restClient = RestClient.builder()
                .baseUrl(propriedades.getUrl())
                .defaultHeader("access_token", propriedades.getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public AsaasDtos.ClienteResponse criarCliente(AsaasDtos.CriarClienteRequest requisicao) {
        verificarConfiguracao();
        try {
            return restClient.post()
                    .uri("/v3/customers")
                    .body(requisicao)
                    .retrieve()
                    .body(AsaasDtos.ClienteResponse.class);
        } catch (Exception ex) {
            throw new NegocioException("Falha ao criar cliente no gateway de pagamento.");
        }
    }

    public AsaasDtos.AssinaturaResponse criarAssinatura(AsaasDtos.CriarAssinaturaRequest requisicao) {
        verificarConfiguracao();
        try {
            return restClient.post()
                    .uri("/v3/subscriptions")
                    .body(requisicao)
                    .retrieve()
                    .body(AsaasDtos.AssinaturaResponse.class);
        } catch (Exception ex) {
            throw new NegocioException("Falha ao criar assinatura no gateway de pagamento.");
        }
    }

    public AsaasDtos.PagamentoResponse criarPagamento(AsaasDtos.CriarPagamentoRequest requisicao) {
        verificarConfiguracao();
        try {
            return restClient.post()
                    .uri("/v3/payments")
                    .body(requisicao)
                    .retrieve()
                    .body(AsaasDtos.PagamentoResponse.class);
        } catch (Exception ex) {
            throw new NegocioException("Falha ao gerar cobranca no gateway de pagamento.");
        }
    }

    public void cancelarAssinatura(String asaasAssinaturaId) {
        verificarConfiguracao();
        try {
            restClient.delete()
                    .uri("/v3/subscriptions/{id}", asaasAssinaturaId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            throw new NegocioException("Falha ao cancelar assinatura no gateway de pagamento.");
        }
    }

    private void verificarConfiguracao() {
        if (!propriedades.configurado()) {
            throw new NegocioException("Os pagamentos ainda nao estao configurados no sistema.");
        }
    }
}
