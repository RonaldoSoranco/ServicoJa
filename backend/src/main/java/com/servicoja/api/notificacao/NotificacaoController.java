package com.servicoja.api.notificacao;

import com.servicoja.infra.PageResposta;
import com.servicoja.infra.seguranca.UsuarioAtual;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificacoes", description = "Notificacoes do usuario")
public class NotificacaoController {

    private final NotificacaoServiceUsuario notificacaoService;
    private final UsuarioAtual usuarioAtual;

    public NotificacaoController(NotificacaoServiceUsuario notificacaoService, UsuarioAtual usuarioAtual) {
        this.notificacaoService = notificacaoService;
        this.usuarioAtual = usuarioAtual;
    }

    @GetMapping
    public PageResposta<NotificacaoDtos.NotificacaoResposta> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return notificacaoService.listar(usuarioAtual.obter(), pagina, tamanho);
    }

    @GetMapping("/nao-lidas")
    public NotificacaoDtos.NaoLidasResposta contarNaoLidas() {
        return new NotificacaoDtos.NaoLidasResposta(notificacaoService.contarNaoLidas(usuarioAtual.obter()));
    }

    @PatchMapping("/{id}/lida")
    public NotificacaoDtos.NotificacaoResposta marcarLida(@PathVariable Long id) {
        return notificacaoService.marcarLida(usuarioAtual.obter(), id);
    }

    @PatchMapping("/lidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarTodasLidas() {
        notificacaoService.marcarTodasLidas(usuarioAtual.obter());
    }
}
