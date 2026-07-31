package com.servicoja.api.avaliacao;

import com.servicoja.infra.PageResposta;
import com.servicoja.infra.seguranca.UsuarioAtual;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avaliacoes")
@Tag(name = "Avaliacoes", description = "Avaliacoes de empresas por clientes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final UsuarioAtual usuarioAtual;

    public AvaliacaoController(AvaliacaoService avaliacaoService, UsuarioAtual usuarioAtual) {
        this.avaliacaoService = avaliacaoService;
        this.usuarioAtual = usuarioAtual;
    }

    @PostMapping("/empresas/{empresaId}")
    @ResponseStatus(HttpStatus.CREATED)
    public AvaliacaoDtos.AvaliacaoResposta avaliar(@PathVariable Long empresaId,
                                                   @Valid @RequestBody AvaliacaoDtos.AvaliacaoRequest requisicao) {
        return avaliacaoService.avaliar(usuarioAtual.obter(), empresaId, requisicao);
    }

    @GetMapping("/empresas/{empresaId}")
    public PageResposta<AvaliacaoDtos.AvaliacaoResposta> listarPorEmpresa(
            @PathVariable Long empresaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return avaliacaoService.listarPorEmpresa(empresaId, pagina, tamanho);
    }

    @GetMapping("/minhas")
    public List<AvaliacaoDtos.AvaliacaoResposta> listarMinhas() {
        return avaliacaoService.listarMinhas(usuarioAtual.obter());
    }
}
