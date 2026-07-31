package com.servicoja.api.admin;

import com.servicoja.api.avaliacao.AvaliacaoDtos;
import com.servicoja.api.avaliacao.AvaliacaoService;
import com.servicoja.api.empresa.EmpresaDtos;
import com.servicoja.api.empresa.EmpresaService;
import com.servicoja.infra.PageResposta;
import com.servicoja.infra.seguranca.UsuarioAtual;
import com.servicoja.infra.servico.LogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administracao", description = "Moderacao e painel administrativo")
public class AdminController {

    private final AdminService adminService;
    private final EmpresaService empresaService;
    private final AvaliacaoService avaliacaoService;
    private final UsuarioAtual usuarioAtual;
    private final LogService logService;

    public AdminController(
            AdminService adminService,
            EmpresaService empresaService,
            AvaliacaoService avaliacaoService,
            UsuarioAtual usuarioAtual,
            LogService logService) {
        this.adminService = adminService;
        this.empresaService = empresaService;
        this.avaliacaoService = avaliacaoService;
        this.usuarioAtual = usuarioAtual;
        this.logService = logService;
    }

    @GetMapping("/estatisticas")
    public AdminDtos.EstatisticasResposta estatisticas() {
        return adminService.estatisticas();
    }

    @GetMapping("/empresas")
    public PageResposta<EmpresaDtos.EmpresaResposta> listarEmpresas(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String uf,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return empresaService.buscarAdministrativo(categoriaId, nome, cidade, uf, pagina, tamanho);
    }

    @PatchMapping("/empresas/{id}/aprovacao")
    public EmpresaDtos.EmpresaResposta aprovarEmpresa(@PathVariable Long id,
                                                      @RequestParam boolean aprovada,
                                                      HttpServletRequest request) {
        EmpresaDtos.EmpresaResposta resposta = empresaService.aprovar(id, aprovada);
        logService.registrar(usuarioAtual.obter(),
                "EMPRESA_" + (aprovada ? "APROVADA" : "REPROVADA"),
                "Empresa id=" + id, request);
        return resposta;
    }

    @GetMapping("/avaliacoes/pendentes")
    public PageResposta<AvaliacaoDtos.AvaliacaoResposta> avaliacoesPendentes(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return avaliacaoService.listarPendentes(pagina, tamanho);
    }

    @PatchMapping("/avaliacoes/{id}/moderacao")
    public AvaliacaoDtos.AvaliacaoResposta moderarAvaliacao(@PathVariable Long id,
                                                            @Valid @RequestBody AvaliacaoDtos.ModeraAvaliacaoRequest requisicao,
                                                            HttpServletRequest request) {
        AvaliacaoDtos.AvaliacaoResposta resposta = avaliacaoService.moderar(id, requisicao);
        logService.registrar(usuarioAtual.obter(),
                "AVALIACAO_MODERADA",
                "Avaliacao id=" + id + " -> " + requisicao.status(), request);
        return resposta;
    }

    @GetMapping("/usuarios")
    public PageResposta<AdminDtos.UsuarioAdminResposta> listarUsuarios(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return adminService.listarUsuarios(pagina, tamanho);
    }

    @GetMapping("/logs")
    public PageResposta<AdminDtos.LogResposta> listarLogs(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return adminService.listarLogs(pagina, tamanho);
    }
}
