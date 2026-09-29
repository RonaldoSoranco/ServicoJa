package com.servicoja.api.favorito;

import com.servicoja.dominio.categoria.Categoria;
import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.excecao.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FavoritoIntegracaoTest {

    @Autowired
    private FavoritoService favoritoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Usuario cliente;
    private Empresa empresaAprovada;

    @BeforeEach
    void preparar() {
        cliente = criarUsuario("Cliente Favorito", "cliente.favorito@teste.com", Perfil.CLIENTE);
        Usuario dono = criarUsuario("Dono Favorito", "dono.favorito@teste.com", Perfil.EMPRESA);
        empresaAprovada = criarEmpresa(dono, true);
    }

    @Test
    void favoritarAdicionaEConsultaEstado() {
        favoritoService.favoritar(cliente, empresaAprovada.getId());

        assertThat(favoritoService.verificarEstado(cliente, empresaAprovada.getId()).favoritado()).isTrue();
        assertThat(favoritoService.listar(cliente, 0, 10).conteudo())
                .anyMatch(f -> f.empresaId().equals(empresaAprovada.getId()));
    }

    @Test
    void favoritarDuasVezesFalha() {
        favoritoService.favoritar(cliente, empresaAprovada.getId());

        assertThatThrownBy(() -> favoritoService.favoritar(cliente, empresaAprovada.getId()))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("ja esta nos seus favoritos");
    }

    @Test
    void naoPodeFavoritarEmpresaNaoAprovada() {
        Usuario dono = criarUsuario("Dono Pendente", "dono.pendente@teste.com", Perfil.EMPRESA);
        Empresa pendente = criarEmpresa(dono, false);

        assertThatThrownBy(() -> favoritoService.favoritar(cliente, pendente.getId()))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("precisa ser aprovada");
    }

    @Test
    void desfavoritarRemoveDosFavoritos() {
        favoritoService.favoritar(cliente, empresaAprovada.getId());

        favoritoService.desfavoritar(cliente, empresaAprovada.getId());

        assertThat(favoritoService.verificarEstado(cliente, empresaAprovada.getId()).favoritado()).isFalse();
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
        nova.setNome("Empresa de Teste Favorito");
        nova.setCidade("Marau");
        nova.setUf("RS");
        nova.setAprovada(aprovada);
        return empresaRepository.save(nova);
    }
}
