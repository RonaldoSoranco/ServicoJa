package com.servicoja.api.favorito;

import com.servicoja.infra.PageResposta;
import com.servicoja.infra.seguranca.UsuarioAtual;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Empresas favoritas do cliente")
public class FavoritoController {

    private final FavoritoService favoritoService;
    private final UsuarioAtual usuarioAtual;

    public FavoritoController(FavoritoService favoritoService, UsuarioAtual usuarioAtual) {
        this.favoritoService = favoritoService;
        this.usuarioAtual = usuarioAtual;
    }

    @PostMapping("/empresas/{empresaId}")
    public FavoritoDtos.MensagemResposta favoritar(@PathVariable Long empresaId) {
        return favoritoService.favoritar(usuarioAtual.obter(), empresaId);
    }

    @DeleteMapping("/empresas/{empresaId}")
    public FavoritoDtos.MensagemResposta remover(@PathVariable Long empresaId) {
        return favoritoService.desfavoritar(usuarioAtual.obter(), empresaId);
    }

    @GetMapping
    public PageResposta<FavoritoDtos.FavoritoResposta> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return favoritoService.listar(usuarioAtual.obter(), pagina, tamanho);
    }

    @GetMapping("/estado")
    public FavoritoDtos.EstadoResposta verificarEstado(@RequestParam Long empresaId) {
        return favoritoService.verificarEstado(usuarioAtual.obter(), empresaId);
    }
}
