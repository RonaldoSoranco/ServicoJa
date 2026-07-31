package com.servicoja.api.empresa;

import com.servicoja.dominio.categoria.Categoria;
import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.excecao.NegocioException;
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
class EmpresaIntegracaoTest {

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void buscaPorCidadeRetornaEmpresasDoSeed() {
        var resultado = empresaService.buscarPublico(null, null, "Marau", "RS", 0, 10);

        assertThat(resultado.totalElementos()).isGreaterThan(0);
    }

    @Test
    void buscaPorCategoriaFiltraResultados() {
        Categoria categoria = categoriaRepository.findAll().stream().findFirst().orElseThrow();

        var resultado = empresaService.buscarPublico(categoria.getId(), null, null, null, 0, 10);

        assertThat(resultado.conteudo()).allSatisfy(empresa ->
                assertThat(empresa.categoria().id()).isEqualTo(categoria.getId()));
    }

    @Test
    void destaqueExigePremiumAtivo() {
        Usuario dono = new Usuario();
        dono.setNome("Dono Destacavel");
        dono.setEmail("dono.destacavel@teste.com");
        dono.setSenha("senhaqualquer");
        dono.setPerfil(Perfil.EMPRESA);
        Usuario donoSalvo = usuarioRepository.save(dono);

        Categoria categoria = categoriaRepository.findAll().stream().findFirst().orElseThrow();
        var criada = empresaService.criar(donoSalvo, new EmpresaDtos.EmpresaRequest(
                "Empresa Gratuita", categoria.getId(), null, null, null, null, null, null,
                null, null, null, null, "Marau", "RS", null, null, null, null, null));

        assertThatThrownBy(() -> empresaService.ativarDestaque(criada.id(), donoSalvo))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Premium");
    }

    @Test
    void empresaGratuitaTemPerfilSimplificado() {
        Usuario dono = new Usuario();
        dono.setNome("Dono Perfil");
        dono.setEmail("dono.perfil@teste.com");
        dono.setSenha("senhaqualquer");
        dono.setPerfil(Perfil.EMPRESA);
        dono = usuarioRepository.save(dono);

        Categoria categoria = categoriaRepository.findAll().stream().findFirst().orElseThrow();
        var criada = empresaService.criar(dono, new EmpresaDtos.EmpresaRequest(
                "Empresa Basica", categoria.getId(), null, "Descricao completa premium",
                null, null, null, null, null, null, null, null, "Marau", "RS", null, null,
                "Seg a Sex", "{\"instagram\":\"@empresa\"}", "https://site.com"));

        assertThat(criada.perfilCompleto()).isFalse();
        assertThat(criada.descricaoCompleta()).isNull();
        assertThat(criada.horarioFuncionamento()).isNull();
        assertThat(criada.site()).isNull();
    }
}
