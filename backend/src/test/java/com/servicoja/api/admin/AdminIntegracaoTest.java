package com.servicoja.api.admin;

import com.servicoja.dominio.categoria.Categoria;
import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminIntegracaoTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void estatisticasContabilizamUsuariosEEmpresasCriados() {
        var antes = adminService.estatisticas();

        Usuario dono = criarUsuario("Dono Estatisticas", "dono.estatisticas@teste.com", Perfil.EMPRESA);
        criarEmpresa(dono, false);

        var depois = adminService.estatisticas();

        assertThat(depois.totalUsuarios()).isEqualTo(antes.totalUsuarios() + 1);
        assertThat(depois.totalEmpresas()).isEqualTo(antes.totalEmpresas() + 1);
        assertThat(depois.empresasPendentes()).isEqualTo(antes.empresasPendentes() + 1);
    }

    @Test
    void listarUsuariosRetornaUsuarioRecemCriado() {
        criarUsuario("Usuario Listagem Admin", "usuario.listagem.admin@teste.com", Perfil.CLIENTE);

        var pagina = adminService.listarUsuarios(0, 100);

        assertThat(pagina.conteudo()).anyMatch(u -> u.email().equals("usuario.listagem.admin@teste.com"));
    }

    @Test
    void listarLogsNaoFalhaQuandoNaoHaLogs() {
        var pagina = adminService.listarLogs(0, 10);

        assertThat(pagina).isNotNull();
    }

    private Usuario criarUsuario(String nome, String email, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha("$2a$10$teste");
        usuario.setPerfil(perfil);
        return usuarioRepository.save(usuario);
    }

    private Empresa criarEmpresa(Usuario dono, boolean aprovada) {
        Categoria categoria = categoriaRepository.findAll().stream().findFirst().orElseThrow();
        Empresa nova = new Empresa();
        nova.setUsuario(dono);
        nova.setCategoria(categoria);
        nova.setNome("Empresa de Teste Admin");
        nova.setCidade("Marau");
        nova.setUf("RS");
        nova.setAprovada(aprovada);
        return empresaRepository.save(nova);
    }
}
