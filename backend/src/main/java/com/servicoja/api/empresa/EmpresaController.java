package com.servicoja.api.empresa;

import com.servicoja.infra.PageResposta;
import com.servicoja.infra.seguranca.UsuarioAtual;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@Tag(name = "Empresas", description = "Busca, perfil e gestao de empresas")
public class EmpresaController {

    private final EmpresaService empresaService;
    private final DesempenhoService desempenhoService;
    private final UsuarioAtual usuarioAtual;

    public EmpresaController(EmpresaService empresaService, DesempenhoService desempenhoService, UsuarioAtual usuarioAtual) {
        this.empresaService = empresaService;
        this.desempenhoService = desempenhoService;
        this.usuarioAtual = usuarioAtual;
    }

    /**
     * Busca publica. {@code latitude}/{@code longitude} (posicao de quem busca) ordenam por
     * proximidade; {@code abertas=true} traz so empresas abertas no momento.
     */
    @GetMapping
    public PageResposta<EmpresaDtos.EmpresaSimplesResposta> buscar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "false") boolean abertas,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        EmpresaDtos.FiltroBusca filtro =
                new EmpresaDtos.FiltroBusca(categoriaId, nome, cidade, uf, latitude, longitude, abertas);
        return empresaService.buscarPublico(filtro, pagina, tamanho);
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

    @PostMapping(value = "/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EmpresaDtos.FotoResposta adicionarFoto(@PathVariable Long id,
                                                  @RequestParam("arquivo") MultipartFile arquivo,
                                                  @RequestParam(required = false) String descricao,
                                                  @RequestParam(required = false) Integer ordem) {
        return empresaService.adicionarFoto(id, usuarioAtual.obter(), arquivo, descricao, ordem);
    }

    @DeleteMapping("/{id}/fotos/{fotoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerFoto(@PathVariable Long id, @PathVariable Long fotoId) {
        empresaService.removerFoto(id, fotoId, usuarioAtual.obter());
    }

    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmpresaDtos.EmpresaResposta atualizarLogo(@PathVariable Long id,
                                                     @RequestParam("arquivo") MultipartFile arquivo) {
        return empresaService.atualizarLogo(id, usuarioAtual.obter(), arquivo);
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

    /** Registra uma interacao com o perfil (publico: visitantes tambem contam no painel). */
    @PostMapping("/{id}/eventos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registrarEvento(@PathVariable Long id,
                                @Valid @RequestBody EmpresaDtos.EventoRequest requisicao,
                                HttpServletRequest request) {
        desempenhoService.registrar(id, requisicao.tipo(), request.getRemoteAddr(),
                usuarioAtual.obterIdOpcional().orElse(null));
    }

    @GetMapping("/{id}/desempenho")
    public EmpresaDtos.DesempenhoResposta desempenho(@PathVariable Long id) {
        return desempenhoService.consultar(id, usuarioAtual.obter());
    }
}
