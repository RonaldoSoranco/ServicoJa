package com.servicoja.api.assinatura;

import com.servicoja.infra.seguranca.UsuarioAtual;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assinaturas")
@Tag(name = "Assinaturas", description = "Assinatura Premium via Asaas")
public class AssinaturaController {

    private final AssinaturaService assinaturaService;
    private final UsuarioAtual usuarioAtual;

    public AssinaturaController(AssinaturaService assinaturaService, UsuarioAtual usuarioAtual) {
        this.assinaturaService = assinaturaService;
        this.usuarioAtual = usuarioAtual;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssinaturaDtos.AssinaturaResposta criar(@Valid @RequestBody AssinaturaDtos.CriarAssinaturaRequest requisicao) {
        return assinaturaService.criar(usuarioAtual.obter(), requisicao);
    }

    @GetMapping("/empresas/{empresaId}")
    public AssinaturaDtos.AssinaturaResposta obterAtiva(@PathVariable Long empresaId) {
        return assinaturaService.obterAssinaturaAtiva(usuarioAtual.obter(), empresaId);
    }

    @DeleteMapping("/{id}")
    public AssinaturaDtos.MensagemResposta cancelar(@PathVariable Long id) {
        return assinaturaService.cancelar(usuarioAtual.obter(), id);
    }
}
