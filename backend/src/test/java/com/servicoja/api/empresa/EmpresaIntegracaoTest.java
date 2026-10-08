package com.servicoja.api.empresa;

import com.servicoja.dominio.avaliacao.Avaliacao;
import com.servicoja.dominio.avaliacao.AvaliacaoRepository;
import com.servicoja.dominio.avaliacao.StatusAvaliacao;
import com.servicoja.dominio.categoria.Categoria;
import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.favorito.Favorito;
import com.servicoja.dominio.favorito.FavoritoRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

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

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Test
    void buscaPorCidadeRetornaEmpresasDoSeed() {
        var resultado = empresaService.buscarPublico(filtro(null, null, "Marau", "RS"), 0, 10);

        assertThat(resultado.totalElementos()).isGreaterThan(0);
    }

    @Test
    void buscaPorCategoriaFiltraResultados() {
        Categoria categoria = categoriaRepository.findAll().stream().findFirst().orElseThrow();

        var resultado = empresaService.buscarPublico(filtro(categoria.getId(), null, null, null), 0, 10);

        assertThat(resultado.conteudo()).allSatisfy(empresa ->
                assertThat(empresa.categoria().id()).isEqualTo(categoria.getId()));
    }

    @Test
    void destaqueExigePremiumAtivo() {
        Usuario dono = criarUsuario("Dono Destacavel", "dono.destacavel@teste.com", Perfil.EMPRESA);

        Categoria categoria = categoriaRepository.findAll().stream().findFirst().orElseThrow();
        var criada = empresaService.criar(dono, new EmpresaDtos.EmpresaRequest(
                "Empresa Gratuita", categoria.getId(), null, null, null, null, null,
                null, null, null, null, "Marau", "RS", null, null, null, null, null));

        assertThatThrownBy(() -> empresaService.ativarDestaque(criada.id(), dono))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Premium");
    }

    @Test
    void empresaGratuitaTemPerfilSimplificado() {
        Usuario dono = criarUsuario("Dono Perfil", "dono.perfil@teste.com", Perfil.EMPRESA);

        Categoria categoria = categoriaRepository.findAll().stream().findFirst().orElseThrow();
        var criada = empresaService.criar(dono, new EmpresaDtos.EmpresaRequest(
                "Empresa Basica", categoria.getId(), null, "Descricao completa premium",
                null, null, null, null, null, null, null, "Marau", "RS", null, null,
                null, "{\"instagram\":\"@empresa\"}", "https://site.com"));

        assertThat(criada.perfilCompleto()).isFalse();
        assertThat(criada.descricaoCompleta()).isNull();
        assertThat(criada.redesSociais()).isNull();
        assertThat(criada.site()).isNull();
    }

    @Test
    void excluirEmpresaComAvaliacoesEFavoritosTiraEmpresaDaPlataforma() {
        Usuario dono = criarUsuario("Dono Exclusao", "dono.exclusao@teste.com", Perfil.EMPRESA);
        Usuario cliente = criarUsuario("Cliente Exclusao", "cliente.exclusao@teste.com", Perfil.CLIENTE);
        Empresa empresa = criarEmpresaAprovada(dono, "Empresa Que Sera Excluida");
        avaliar(cliente, empresa);
        favoritar(cliente, empresa);

        empresaService.excluir(empresa.getId(), dono);

        Empresa registro = empresaRepository.findById(empresa.getId()).orElseThrow();
        assertThat(registro.getExcluidaEm()).isNotNull();
        assertThat(registro.getAprovada()).isFalse();
        assertThat(registro.getTelefone()).isNull();
        assertThat(avaliacaoRepository.countByEmpresaIdAndStatus(empresa.getId(), StatusAvaliacao.APROVADA)).isZero();
        assertThat(favoritoRepository.existsByUsuarioIdAndEmpresaId(cliente.getId(), empresa.getId())).isFalse();

        assertThat(empresaService.buscarPublico(filtro(null, "Empresa Que Sera Excluida", null, null), 0, 10).conteudo())
                .isEmpty();
        assertThat(empresaService.buscarAdministrativo(null, "Empresa Que Sera Excluida", null, null, 0, 10).conteudo())
                .isEmpty();
        assertThat(empresaService.listarMinhas(dono)).isEmpty();
        assertThatThrownBy(() -> empresaService.detalhar(empresa.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void empresaExcluidaNaoPodeSerAprovadaNovamente() {
        Usuario dono = criarUsuario("Dono Reaprovacao", "dono.reaprovacao@teste.com", Perfil.EMPRESA);
        Empresa empresa = criarEmpresaAprovada(dono, "Empresa Reaprovacao");
        empresaService.excluir(empresa.getId(), dono);

        assertThatThrownBy(() -> empresaService.aprovar(empresa.getId(), true))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void usuarioNaoPodeExcluirEmpresaDeOutroUsuario() {
        Usuario dono = criarUsuario("Dono Protegido", "dono.protegido@teste.com", Perfil.EMPRESA);
        Usuario outro = criarUsuario("Outro Dono", "outro.dono@teste.com", Perfil.EMPRESA);
        Empresa empresa = criarEmpresaAprovada(dono, "Empresa Protegida");

        assertThatThrownBy(() -> empresaService.excluir(empresa.getId(), outro))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("permissao");
        assertThat(empresaRepository.findById(empresa.getId()).orElseThrow().getExcluidaEm()).isNull();
    }

    @Test
    void alterarHorariosELocalizacaoNaoTiraAEmpresaDoAr() {
        Usuario dono = criarUsuario("Dono Horarios", "dono.horarios@teste.com", Perfil.EMPRESA);
        Empresa empresa = criarEmpresaAprovada(dono, "Empresa Horarios");
        var horarios = List.of(
                new EmpresaDtos.HorarioDto(1, LocalTime.of(8, 0), LocalTime.of(12, 0)),
                new EmpresaDtos.HorarioDto(1, LocalTime.of(13, 30), LocalTime.of(18, 0)));

        var atualizada = empresaService.atualizar(empresa.getId(), dono, requisicao(
                empresa, "Empresa Horarios", new BigDecimal("-28.4489"), new BigDecimal("-52.1992"), horarios));

        assertThat(atualizada.aprovada()).isTrue();
        assertThat(atualizada.horarios()).hasSize(2);
        assertThat(atualizada.latitude()).isEqualByComparingTo("-28.4489");
    }

    @Test
    void alterarONomeExigeNovaAprovacao() {
        Usuario dono = criarUsuario("Dono Renomeia", "dono.renomeia@teste.com", Perfil.EMPRESA);
        Empresa empresa = criarEmpresaAprovada(dono, "Nome Antigo");

        var atualizada = empresaService.atualizar(empresa.getId(), dono, requisicao(empresa, "Nome Novo", null, null, null));

        assertThat(atualizada.aprovada()).isFalse();
    }

    @Test
    void horariosNulosMantemOsAtuais() {
        Usuario dono = criarUsuario("Dono Mantem", "dono.mantem@teste.com", Perfil.EMPRESA);
        Empresa empresa = criarEmpresaAprovada(dono, "Empresa Mantem");
        var horarios = List.of(new EmpresaDtos.HorarioDto(2, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        empresaService.atualizar(empresa.getId(), dono, requisicao(empresa, "Empresa Mantem", null, null, horarios));

        var atualizada = empresaService.atualizar(empresa.getId(), dono, requisicao(empresa, "Empresa Mantem", null, null, null));

        assertThat(atualizada.horarios()).hasSize(1);
    }

    @Test
    void recusaHorariosSobrepostosOuInvertidos() {
        Usuario dono = criarUsuario("Dono Invalido", "dono.invalido@teste.com", Perfil.EMPRESA);
        Empresa empresa = criarEmpresaAprovada(dono, "Empresa Invalida");
        var sobrepostos = List.of(
                new EmpresaDtos.HorarioDto(3, LocalTime.of(8, 0), LocalTime.of(12, 0)),
                new EmpresaDtos.HorarioDto(3, LocalTime.of(11, 0), LocalTime.of(14, 0)));
        var invertido = List.of(new EmpresaDtos.HorarioDto(4, LocalTime.of(18, 0), LocalTime.of(8, 0)));

        assertThatThrownBy(() -> empresaService.atualizar(empresa.getId(), dono,
                requisicao(empresa, "Empresa Invalida", null, null, sobrepostos)))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("sobrepor");
        assertThatThrownBy(() -> empresaService.atualizar(empresa.getId(), dono,
                requisicao(empresa, "Empresa Invalida", null, null, invertido)))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("fechamento");
    }

    private EmpresaDtos.EmpresaRequest requisicao(Empresa empresa, String nome, BigDecimal latitude,
                                                  BigDecimal longitude, List<EmpresaDtos.HorarioDto> horarios) {
        return new EmpresaDtos.EmpresaRequest(
                nome, empresa.getCategoria().getId(), null, null, empresa.getTelefone(), null, null,
                null, null, null, null, "Marau", "RS", latitude, longitude, horarios, null, null);
    }

    private static EmpresaDtos.FiltroBusca filtro(Long categoriaId, String nome, String cidade, String uf) {
        return new EmpresaDtos.FiltroBusca(categoriaId, nome, cidade, uf, null, null, false);
    }

    private Usuario criarUsuario(String nome, String email, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha("$2a$10$teste");
        usuario.setPerfil(perfil);
        return usuarioRepository.save(usuario);
    }

    private Empresa criarEmpresaAprovada(Usuario dono, String nome) {
        Empresa empresa = new Empresa();
        empresa.setUsuario(dono);
        empresa.setCategoria(categoriaRepository.findAll().stream().findFirst().orElseThrow());
        empresa.setNome(nome);
        empresa.setTelefone("(54) 99999-0000");
        empresa.setCidade("Marau");
        empresa.setUf("RS");
        empresa.setAprovada(true);
        return empresaRepository.save(empresa);
    }

    private void avaliar(Usuario cliente, Empresa empresa) {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(cliente);
        avaliacao.setEmpresa(empresa);
        avaliacao.setNota(5);
        avaliacao.setStatus(StatusAvaliacao.APROVADA);
        avaliacaoRepository.save(avaliacao);
    }

    private void favoritar(Usuario cliente, Empresa empresa) {
        Favorito favorito = new Favorito();
        favorito.setUsuario(cliente);
        favorito.setEmpresa(empresa);
        favoritoRepository.save(favorito);
    }
}
