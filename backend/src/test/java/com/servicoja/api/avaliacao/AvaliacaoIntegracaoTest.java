package com.servicoja.api.avaliacao;

import com.servicoja.dominio.avaliacao.AvaliacaoRepository;
import com.servicoja.dominio.avaliacao.StatusAvaliacao;
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
class AvaliacaoIntegracaoTest {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    private Usuario cliente;
    private Empresa empresa;

    @BeforeEach
    void preparar() {
        cliente = criarUsuario("Cliente Avaliacao", "cliente.avaliacao@teste.com", Perfil.CLIENTE);
        Usuario dono = criarUsuario("Dono Avaliacao", "dono.avaliacao@teste.com", Perfil.EMPRESA);
        empresa = criarEmpresa(dono);
    }

    @Test
    void clienteAvaliaEmpresaComSucesso() {
        var resposta = avaliacaoService.avaliar(cliente, empresa.getId(),
                new AvaliacaoDtos.AvaliacaoRequest(5, "Excelente atendimento!"));

        assertThat(resposta.nota()).isEqualTo(5);
        assertThat(resposta.status()).isEqualTo(StatusAvaliacao.PENDENTE);
    }

    @Test
    void mesmoUsuarioNaoPodeAvaliarDuasVezesAMesmaEmpresa() {
        avaliacaoService.avaliar(cliente, empresa.getId(), new AvaliacaoDtos.AvaliacaoRequest(5, "Muito bom"));

        assertThatThrownBy(() -> avaliacaoService.avaliar(cliente, empresa.getId(),
                new AvaliacaoDtos.AvaliacaoRequest(4, "Nova avaliacao")))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("ja avaliou esta empresa");
    }

    @Test
    void moderacaoAprovaEUmaMediaSobreEmpresa() {
        avaliacaoService.avaliar(cliente, empresa.getId(), new AvaliacaoDtos.AvaliacaoRequest(4, "Bom servico"));

        Long avaliacaoId = avaliacaoRepository.findAll().stream()
                .filter(a -> a.getEmpresa().getId().equals(empresa.getId()))
                .findFirst()
                .orElseThrow()
                .getId();

        avaliacaoService.moderar(avaliacaoId, new AvaliacaoDtos.ModeraAvaliacaoRequest(StatusAvaliacao.APROVADA));

        Empresa atualizada = empresaRepository.findById(empresa.getId()).orElseThrow();
        assertThat(atualizada.getTotalAvaliacoes()).isEqualTo(1);
        assertThat(atualizada.getMediaAvaliacoes().doubleValue()).isEqualTo(4.0);
    }

    private Usuario criarUsuario(String nome, String email, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha("$2a$10$teste");
        usuario.setPerfil(perfil);
        return usuarioRepository.save(usuario);
    }

    private Empresa criarEmpresa(Usuario dono) {
        Categoria categoria = categoriaRepository.findAll().stream().findFirst().orElseThrow();
        Empresa nova = new Empresa();
        nova.setUsuario(dono);
        nova.setCategoria(categoria);
        nova.setNome("Empresa de Teste Avaliacao");
        nova.setCidade("Marau");
        nova.setUf("RS");
        nova.setAprovada(true);
        return empresaRepository.save(nova);
    }
}
