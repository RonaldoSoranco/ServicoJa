package com.servicoja.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sobe o Tomcat de verdade para validar que, atras de um proxy, os limites de requisicoes por IP
 * usam o IP real do cliente (X-Forwarded-For) e nao o IP do proxy, que seria o mesmo para todos.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class IpDoClienteAtrasDeProxyIntegracaoTest {

    private static final int LIMITE_RECUPERACAO_SENHA = 3;

    private final HttpClient http = HttpClient.newHttpClient();

    @LocalServerPort
    private int porta;

    @Test
    void limiteDeRequisicoesEContadoPorClienteENaoPorProxy() throws Exception {
        for (int i = 0; i < LIMITE_RECUPERACAO_SENHA; i++) {
            assertThat(recuperarSenhaComo("203.0.113.10")).isEqualTo(200);
        }

        assertThat(recuperarSenhaComo("203.0.113.10")).isEqualTo(429);
        assertThat(recuperarSenhaComo("203.0.113.20")).isEqualTo(200);
    }

    private int recuperarSenhaComo(String ipDoCliente) throws Exception {
        HttpRequest requisicao = HttpRequest.newBuilder(
                        URI.create("http://127.0.0.1:" + porta + "/api/auth/recuperar-senha"))
                .header("Content-Type", "application/json")
                .header("X-Forwarded-For", ipDoCliente)
                .POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"ninguem.proxy@teste.com\"}"))
                .build();
        return http.send(requisicao, HttpResponse.BodyHandlers.discarding()).statusCode();
    }
}
