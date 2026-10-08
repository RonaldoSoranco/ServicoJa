package com.servicoja.seguranca;

import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regras de acesso verificadas pela cadeia de filtros real (JWT + Spring Security + MVC).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SegurancaHttpIntegracaoTest {

    private static final String CATEGORIA_JSON = """
            {"nome":"Categoria Criada Pelo Teste","descricao":"Teste","icone":"teste"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Value("${servico-ja.asaas.webhook-token}")
    private String tokenWebhook;

    @Test
    void listagemDeCategoriasEPublica() throws Exception {
        mockMvc.perform(get("/api/categorias")).andExpect(status().isOk());
    }

    @Test
    void visitanteNaoPodeCriarCategoria() throws Exception {
        mockMvc.perform(post("/api/categorias").contentType(APPLICATION_JSON).content(CATEGORIA_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNaoPodeCriarAlterarOuExcluirCategorias() throws Exception {
        String token = bearer(criarUsuario(Perfil.CLIENTE, "cliente.categorias@teste.com"));
        Long id = categoriaRepository.findAll().getFirst().getId();

        mockMvc.perform(post("/api/categorias").header(AUTHORIZATION, token)
                        .contentType(APPLICATION_JSON).content(CATEGORIA_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/categorias/{id}", id).header(AUTHORIZATION, token)
                        .contentType(APPLICATION_JSON).content(CATEGORIA_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/categorias/{id}/atividade", id).param("ativa", "false")
                        .header(AUTHORIZATION, token))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/categorias/{id}", id).header(AUTHORIZATION, token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/categorias/todas").header(AUTHORIZATION, token))
                .andExpect(status().isForbidden());
    }

    @Test
    void empresaNaoPodeCriarCategoria() throws Exception {
        String token = bearer(criarUsuario(Perfil.EMPRESA, "empresa.categorias@teste.com"));

        mockMvc.perform(post("/api/categorias").header(AUTHORIZATION, token)
                        .contentType(APPLICATION_JSON).content(CATEGORIA_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorPodeCriarCategoria() throws Exception {
        String token = bearer(criarUsuario(Perfil.ADMIN, "admin.categorias@teste.com"));

        mockMvc.perform(post("/api/categorias").header(AUTHORIZATION, token)
                        .contentType(APPLICATION_JSON).content(CATEGORIA_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void logoEnviadoNoFormularioDaEmpresaEIgnorado() throws Exception {
        Usuario dono = criarUsuario(Perfil.EMPRESA, "dono.logo@teste.com");
        Empresa empresa = criarEmpresa(dono);
        String corpo = """
                {"nome":"Empresa Logo","categoriaId":%d,"cidade":"Marau","uf":"RS",
                 "logoUrl":"/uploads/empresas/999/fotos/arquivo-de-outra-empresa.jpg"}
                """.formatted(empresa.getCategoria().getId());

        mockMvc.perform(put("/api/empresas/{id}", empresa.getId()).header(AUTHORIZATION, bearer(dono))
                        .contentType(APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk());

        assertThat(empresaRepository.findById(empresa.getId()).orElseThrow().getLogoUrl()).isNull();
    }

    @Test
    void contaDesativadaPerdeAcessoMesmoComTokenAindaValido() throws Exception {
        Usuario usuario = criarUsuario(Perfil.CLIENTE, "cliente.desativado@teste.com");
        String token = bearer(usuario);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        mockMvc.perform(get("/api/auth/me").header(AUTHORIZATION, token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exclusaoDeContaExigeAutenticacao() throws Exception {
        mockMvc.perform(post("/api/auth/excluir-conta").contentType(APPLICATION_JSON).content("{\"senha\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void webhookDoAsaasExigeOTokenConfigurado() throws Exception {
        String evento = "{\"event\":\"EVENTO_SEM_EFEITO\"}";

        mockMvc.perform(post("/api/asaas/webhook").contentType(APPLICATION_JSON).content(evento))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/asaas/webhook").header("asaas-access-token", "token-errado")
                        .contentType(APPLICATION_JSON).content(evento))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/asaas/webhook").header("asaas-access-token", tokenWebhook)
                        .contentType(APPLICATION_JSON).content(evento))
                .andExpect(status().isOk());
    }

    @Test
    void visitantePodeRegistrarInteracaoMasNaoVerODesempenho() throws Exception {
        Empresa empresa = criarEmpresa(criarUsuario(Perfil.EMPRESA, "dono.eventos@teste.com"));
        empresa.setAprovada(true);
        empresaRepository.save(empresa);

        mockMvc.perform(post("/api/empresas/{id}/eventos", empresa.getId())
                        .contentType(APPLICATION_JSON).content("{\"tipo\":\"CLIQUE_WHATSAPP\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/empresas/{id}/desempenho", empresa.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buscaRecusaCoordenadasInvalidas() throws Exception {
        mockMvc.perform(get("/api/empresas").param("latitude", "-28.4").param("longitude", "500"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/empresas").param("latitude", "-28.4"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void arquivoDeUploadInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/uploads/empresas/999/fotos/nao-existe.jpg"))
                .andExpect(status().isNotFound());
    }

    private Usuario criarUsuario(Perfil perfil, String email) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario " + perfil);
        usuario.setEmail(email);
        usuario.setSenha("$2a$10$teste");
        usuario.setPerfil(perfil);
        return usuarioRepository.save(usuario);
    }

    private Empresa criarEmpresa(Usuario dono) {
        Empresa empresa = new Empresa();
        empresa.setUsuario(dono);
        empresa.setCategoria(categoriaRepository.findAll().getFirst());
        empresa.setNome("Empresa Logo");
        empresa.setCidade("Marau");
        empresa.setUf("RS");
        return empresaRepository.save(empresa);
    }

    private String bearer(Usuario usuario) {
        return "Bearer " + jwtService.gerarTokenAcesso(usuario.getId(), usuario.getEmail(), usuario.getPerfil());
    }
}
