package com.servicoja.api.empresa;

import com.servicoja.infra.PageResposta;
import com.servicoja.infra.seguranca.UsuarioAtual;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@Tag(name = "Empresas", description = "Busca, perfil e gestao de empresas")
public class EmpresaController {

    private final EmpresaService empresaService;
    private final UsuarioAtual usuarioAtual;

    public EmpresaController(EmpresaService empresaService, UsuarioAtual usuarioAtual) {
        this.empresaService = empresaService;
        this.usuarioAtual = usuarioAtual;
    }

    @GetMapping
    public PageResposta<EmpresaDtos.EmpresaSimplesResposta> buscar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String uf,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return empresaService.buscarPublico(categoriaId, nome, cidade, uf, pagina, tamanho);
    }

    @GetMapping("/{id}")
    public EmpresaDtos.EmpresaResposta detalhar(@PathVariable Long id) {
        return empresaService.detalhar(id);
    }

    @GetMapping("/minhas")
    public List<EmpresaDtos.EmpresaResposta> listarMinhas() {
        return empresaService.listarMinhas(usuarioAtual.obter());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpresaDtos.EmpresaResposta criar(@Valid @RequestBody EmpresaDtos.EmpresaRequest requisicao) {
        return empresaService.criar(usuarioAtual.obter(), requisicao);
    }

    @PutMapping("/{id}")
    public EmpresaDtos.EmpresaResposta atualizar(@PathVariable Long id,
                                                 @Valid @RequestBody EmpresaDtos.EmpresaRequest requisicao) {
        return empresaService.atualizar(id, usuarioAtual.obter(), requisicao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        empresaService.excluir(id, usuarioAtual.obter());
    }

    @PostMapping("/{id}/fotos")
    @ResponseStatus(HttpStatus.CREATED)
    public EmpresaDtos.FotoResposta adicionarFoto(@PathVariable Long id,
                                                  @Valid @RequestBody EmpresaDtos.FotoRequest requisicao) {
        return empresaService.adicionarFoto(id, usuarioAtual.obter(), requisicao);
    }

    @DeleteMapping("/{id}/fotos/{fotoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerFoto(@PathVariable Long id, @PathVariable Long fotoId) {
        empresaService.removerFoto(id, fotoId, usuarioAtual.obter());
    }

    @PostMapping("/{id}/portfolios")
    @ResponseStatus(HttpStatus.CREATED)
    public EmpresaDtos.PortfolioResposta adicionarPortfolio(@PathVariable Long id,
                                                            @Valid @RequestBody EmpresaDtos.PortfolioRequest requisicao) {
        return empresaService.adicionarPortfolio(id, usuarioAtual.obter(), requisicao);
    }

    @DeleteMapping("/{id}/portfolios/{portfolioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerPortfolio(@PathVariable Long id, @PathVariable Long portfolioId) {
        empresaService.removerPortfolio(id, portfolioId, usuarioAtual.obter());
    }

    @PostMapping("/{id}/destaque")
    public EmpresaDtos.MensagemResposta ativarDestaque(@PathVariable Long id) {
        return empresaService.ativarDestaque(id, usuarioAtual.obter());
    }

    @DeleteMapping("/{id}/destaque")
    public EmpresaDtos.MensagemResposta removerDestaque(@PathVariable Long id) {
        return empresaService.removerDestaque(id, usuarioAtual.obter());
    }
}
