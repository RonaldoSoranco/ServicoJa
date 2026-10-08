package com.servicoja.api.empresa;

import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.evento.TipoEventoEmpresa;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DesempenhoIntegracaoTest {

    @Autowired
    private DesempenhoService desempenhoService;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Usuario dono;
    private Empresa empresa;

    @BeforeEach
    void preparar() {
        dono = criarUsuario(Perfil.EMPRESA);
        empresa = criarEmpresa(dono, true);
    }

    @Test
    void contaCadaPessoaUmaVezPorTipoNoIntervalo() {
        String ip = ipUnico();
        desempenhoService.registrar(empresa.getId(), TipoEventoEmpresa.VISUALIZACAO, ip, null);
        desempenhoService.registrar(empresa.getId(), TipoEventoEmpresa.VISUALIZACAO, ip, null);
        desempenhoService.registrar(empresa.getId(), TipoEventoEmpresa.VISUALIZACAO, ipUnico(), null);
        desempenhoService.registrar(empresa.getId(), TipoEventoEmpresa.CLIQUE_WHATSAPP, ip, null);

        var desempenho = desempenhoService.consultar(empresa.getId(), dono);

        assertThat(desempenho.dias()).isEqualTo(30);
        assertThat(desempenho.visualizacoes().atual()).isEqualTo(2);
        assertThat(desempenho.cliquesWhatsapp().atual()).isEqualTo(1);
        assertThat(desempenho.cliquesLigar().atual()).isZero();
        assertThat(desempenho.visualizacoes().anterior()).isZero();
    }

    @Test
    void acessosDoProprioDonoNaoContam() {
        desempenhoService.registrar(empresa.getId(), TipoEventoEmpresa.VISUALIZACAO, ipUnico(), dono.getId());

        assertThat(desempenhoService.consultar(empresa.getId(), dono).visualizacoes().atual()).isZero();
    }

    @Test
    void somenteODonoOuAdminVeemODesempenho() {
        Usuario outro = criarUsuario(Perfil.CLIENTE);
        Usuario admin = criarUsuario(Perfil.ADMIN);

        assertThatThrownBy(() -> desempenhoService.consultar(empresa.getId(), outro))
                .isInstanceOf(NegocioException.class);
        assertThat(desempenhoService.consultar(empresa.getId(), admin)).isNotNull();
    }

    @Test
    void naoRegistraEventosDeEmpresaNaoAprovada() {
        Empresa pendente = criarEmpresa(dono, false);

        assertThatThrownBy(() ->
                desempenhoService.registrar(pendente.getId(), TipoEventoEmpresa.VISUALIZACAO, ipUnico(), null))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private String ipUnico() {
        return "teste-" + UUID.randomUUID();
    }

    private Usuario criarUsuario(Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario " + perfil);
        usuario.setEmail(UUID.randomUUID() + "@desempenho.teste");
        usuario.setSenha("$2a$10$teste");
        usuario.setPerfil(perfil);
        return usuarioRepository.save(usuario);
    }

    private Empresa criarEmpresa(Usuario dono, boolean aprovada) {
        Empresa nova = new Empresa();
        nova.setUsuario(dono);
        nova.setCategoria(categoriaRepository.findAll().getFirst());
        nova.setNome("Empresa Desempenho");
        nova.setCidade("Marau");
        nova.setUf("RS");
        nova.setAprovada(aprovada);
        return empresaRepository.save(nova);
    }
}
