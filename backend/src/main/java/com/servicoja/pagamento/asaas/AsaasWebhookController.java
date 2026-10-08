package com.servicoja.pagamento.asaas;

import com.servicoja.api.assinatura.AssinaturaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/asaas")
@Tag(name = "Asaas", description = "Webhooks do gateway de pagamento")
public class AsaasWebhookController {

    /** Cabecalho em que o Asaas envia o token de autenticacao cadastrado junto com o webhook. */
    static final String CABECALHO_TOKEN = "asaas-access-token";

    private static final Logger LOGGER = LoggerFactory.getLogger(AsaasWebhookController.class);

    private final AssinaturaService assinaturaService;
    private final AsaasPropriedades propriedades;

    public AsaasWebhookController(AssinaturaService assinaturaService, AsaasPropriedades propriedades) {
        this.assinaturaService = assinaturaService;
        this.propriedades = propriedades;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String corpo,
                                        @RequestHeader(value = CABECALHO_TOKEN, required = false) String token) {
        verificarToken(token);
        assinaturaService.processarWebhook(corpo);
        return ResponseEntity.ok().build();
    }

    private void verificarToken(String tokenRecebido) {
        String tokenEsperado = propriedades.getWebhookToken();
        if (tokenEsperado == null || tokenEsperado.isBlank()) {
            LOGGER.error("Webhook do Asaas recebido, mas ASAAS_WEBHOOK_TOKEN nao esta configurado.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Token do webhook nao configurado no servidor.");
        }
        if (tokenRecebido == null || !tokensIguais(tokenEsperado, tokenRecebido)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token do webhook ausente ou invalido.");
        }
    }

    /**
     * Compara os resumos SHA-256 em tempo constante, para que nem o conteudo nem o tamanho
     * do token esperado possam ser deduzidos pelo tempo de resposta.
     */
    private boolean tokensIguais(String esperado, String recebido) {
        return MessageDigest.isEqual(resumo(esperado), resumo(recebido));
    }

    private byte[] resumo(String valor) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponivel na JVM.", ex);
        }
    }
}
