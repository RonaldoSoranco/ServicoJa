package com.servicoja.pagamento.asaas;

import com.servicoja.api.assinatura.AssinaturaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/asaas")
@Tag(name = "Asaas", description = "Webhooks do gateway de pagamento")
public class AsaasWebhookController {

    private final AssinaturaService assinaturaService;
    private final AsaasPropriedades propriedades;

    public AsaasWebhookController(AssinaturaService assinaturaService, AsaasPropriedades propriedades) {
        this.assinaturaService = assinaturaService;
        this.propriedades = propriedades;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String corpo,
                                        @RequestHeader(value = "asaas-signature", required = false) String assinatura) {
        verificarAssinatura(corpo, assinatura);
        assinaturaService.processarWebhook(corpo);
        return ResponseEntity.ok().build();
    }

    private void verificarAssinatura(String corpo, String assinaturaRecebida) {
        String segredo = propriedades.getWebhookSegredo();
        if (segredo == null || segredo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Segredo do webhook nao configurado no servidor.");
        }
        if (assinaturaRecebida == null || !assinaturaRecebida.startsWith("sha256=")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Assinatura do webhook ausente ou invalida.");
        }
        String esperado = "sha256=" + calcularHmac(segredo, corpo);
        if (!MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                assinaturaRecebida.trim().getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Assinatura do webhook invalida.");
        }
    }

    private String calcularHmac(String segredo, String corpo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(corpo.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao validar assinatura do webhook.");
        }
    }
}
