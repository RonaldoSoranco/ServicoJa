package com.servicoja.api.auth;

import com.servicoja.api.avaliacao.AvaliacaoDtos;
import com.servicoja.api.avaliacao.AvaliacaoService;
import com.servicoja.api.empresa.EmpresaDtos;
import com.servicoja.api.empresa.EmpresaService;
import com.servicoja.dominio.assinatura.Assinatura;
import com.servicoja.dominio.assinatura.AssinaturaRepository;
import com.servicoja.dominio.assinatura.StatusAssinatura;
import com.servicoja.dominio.assinatura.TipoAssinatura;
import com.servicoja.dominio.avaliacao.AvaliacaoRepository;
import com.servicoja.dominio.avaliacao.StatusAvaliacao;
import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.favorito.Favorito;
import com.servicoja.dominio.favorito.FavoritoRepository;
import com.servicoja.dominio.notificacao.NotificacaoRepository;
import com.servicoja.dominio.seguranca.TokenRefreshRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.excecao.CredenciaisInvalidasException;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.seguranca.LimitadorRequisicoes;
import com.servicoja.pagamento.asaas.AsaasCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExclusaoContaIntegracaoTest {

    private static final String SENHA = "senha-do-usuario-123";
    private static final String IP = "127.0.0.1";

    @MockitoBean
    private AsaasCliente asaasCliente;

    @Autowired
    private ExclusaoContaService exclusaoContaService;

    @Autowired
    private AuthService authService;

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private TokenRefreshRepository tokenRefreshRepository;

    @Autowired
    private AssinaturaRepository assinaturaRepository;

    @Autowired
    private PasswordEncoder codificador;

    @Autowired
    private LimitadorRequisicoes limitador;

    @BeforeEach
    void limparLimiteDeCadastro() {
        limitador.limpar("cadastro:" + IP);
    }

    @Test
    void senhaIncorretaNaoExcluiAConta() {
        Usuario cliente = cadastrarCliente("cliente.senha.errada@teste.com");

        assertThatThrownBy(() -> exclusaoContaService.excluir(cliente, new AuthDtos.ExcluirContaRequest("senhaErrada")))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Senha incorreta");
        assertThat(usuarioRepository.findById(cliente.getId()).orElseThrow().getExcluidoEm()).isNull();
    }

    @Test
    void administradorNaoPodeExcluirAPropriaConta() {
        Usuario admin = new Usuario();
        admin.setNome("Admin Teste");
        admin.setEmail("admin.exclusao@teste.com");
        admin.setSenha(codificador.encode(SENHA));
        admin.setPerfil(Perfil.ADMIN);
        usuarioRepository.save(admin);

        assertThatThrownBy(() -> exclusaoContaService.excluir(admin, new AuthDtos.ExcluirContaRequest(SENHA)))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("administrador");
    }

    @Test
    void clienteExcluidoTemDadosAnonimizadosEPerdeOAcesso() {
        Usuario cliente = cadastrarCliente("cliente.excluido@teste.com");
        Usuario outroCliente = cadastrarCliente("outro.cliente@teste.com");
        Empresa empresa = criarEmpresaAprovada("dono.avaliado@teste.com", "Empresa Avaliada");
        avaliarEAprovar(cliente, empresa, 1);
        avaliarEAprovar(outroCliente, empresa, 5);
        favoritar(cliente, empresa);

        exclusaoContaService.excluir(cliente, new AuthDtos.ExcluirContaRequest(SENHA));

        Usuario anonimizado = usuarioRepository.findById(cliente.getId()).orElseThrow();
        assertThat(anonimizado.getExcluidoEm()).isNotNull();
        assertThat(anonimizado.getAtivo()).isFalse();
        assertThat(anonimizado.getNome()).isEqualTo("Conta excluida");
        assertThat(anonimizado.getEmail()).doesNotContain("cliente.excluido");
        assertThat(anonimizado.getTelefone()).isNull();

        assertThat(avaliacaoRepository.findByUsuarioIdOrderByCriadoEmDesc(cliente.getId())).isEmpty();
        Empresa recalculada = empresaRepository.findById(empresa.getId()).orElseThrow();
        assertThat(recalculada.getTotalAvaliacoes()).isEqualTo(1);
        assertThat(recalculada.getMediaAvaliacoes()).isEqualByComparingTo("5.0");
        assertThat(favoritoRepository.existsByUsuarioIdAndEmpresaId(cliente.getId(), empresa.getId())).isFalse();
        assertThat(notificacaoRepository.countByUsuarioIdAndLidaFalse(cliente.getId())).isZero();
        assertThat(tokenRefreshRepository.findAllByUsuarioIdAndRevogadoFalse(cliente.getId())).isEmpty();

        assertThatThrownBy(() -> authService.login(new AuthDtos.LoginRequest("cliente.excluido@teste.com", SENHA), IP))
                .isInstanceOf(CredenciaisInvalidasException.class);
        assertThatNoException().isThrownBy(() -> cadastrarCliente("cliente.excluido@teste.com"));
    }

    @Test
    void empresaDoUsuarioExcluidoSaiDaPlataformaESuaAssinaturaECancelada() {
        Usuario dono = cadastrarEmpresa("dono.premium@teste.com", "Empresa Premium Excluida");
        Empresa empresa = empresaRepository.findByUsuarioIdAndExcluidaEmIsNull(dono.getId()).getFirst();
        Assinatura assinatura = criarAssinaturaAtiva(dono, empresa, "sub_teste_123");

        exclusaoContaService.excluir(dono, new AuthDtos.ExcluirContaRequest(SENHA));

        verify(asaasCliente).cancelarAssinatura("sub_teste_123");
        assertThat(assinaturaRepository.findById(assinatura.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusAssinatura.CANCELADA);
        Empresa removida = empresaRepository.findById(empresa.getId()).orElseThrow();
        assertThat(removida.getExcluidaEm()).isNotNull();
        assertThat(removida.getPremiumAtivo()).isFalse();
        var filtro = new EmpresaDtos.FiltroBusca(null, "Empresa Premium Excluida", null, null, null, null, false);
        assertThat(empresaService.buscarPublico(filtro, 0, 10).conteudo()).isEmpty();
    }

    @Test
    void falhaAoCancelarAssinaturaNoGatewayInterrompeAExclusao() {
        doThrow(new NegocioException("Falha ao cancelar assinatura no gateway de pagamento."))
                .when(asaasCliente).cancelarAssinatura(anyString());
        Usuario dono = cadastrarEmpresa("dono.gateway.fora@teste.com", "Empresa Gateway Fora");
        Empresa empresa = empresaRepository.findByUsuarioIdAndExcluidaEmIsNull(dono.getId()).getFirst();
        criarAssinaturaAtiva(dono, empresa, "sub_teste_456");

        assertThatThrownBy(() -> exclusaoContaService.excluir(dono, new AuthDtos.ExcluirContaRequest(SENHA)))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("gateway");
        assertThat(usuarioRepository.findById(dono.getId()).orElseThrow().getExcluidoEm()).isNull();
        assertThat(empresaRepository.findById(empresa.getId()).orElseThrow().getExcluidaEm()).isNull();
    }

    private Usuario cadastrarCliente(String email) {
        var sessao = authService.cadastrarCliente(
                new AuthDtos.CadastroClienteRequest("Cliente Teste", email, SENHA, "(54) 99999-1234"), IP);
        return usuarioRepository.findById(sessao.usuario().id()).orElseThrow();
    }

    private Usuario cadastrarEmpresa(String email, String nomeEmpresa) {
        Long categoriaId = categoriaRepository.findAll().getFirst().getId();
        var sessao = authService.cadastrarEmpresa(new AuthDtos.CadastroEmpresaRequest(
                "Dono Teste", email, SENHA, null, nomeEmpresa, categoriaId, "Marau", "RS", null), IP);
        return usuarioRepository.findById(sessao.usuario().id()).orElseThrow();
    }

    private Empresa criarEmpresaAprovada(String emailDono, String nome) {
        Usuario dono = cadastrarEmpresa(emailDono, nome);
        Empresa empresa = empresaRepository.findByUsuarioIdAndExcluidaEmIsNull(dono.getId()).getFirst();
        empresa.setAprovada(true);
        return empresaRepository.save(empresa);
    }

    private void avaliarEAprovar(Usuario cliente, Empresa empresa, int nota) {
        var avaliacao = avaliacaoService.avaliar(cliente, empresa.getId(), new AvaliacaoDtos.AvaliacaoRequest(nota, "Teste"));
        avaliacaoService.moderar(avaliacao.id(), new AvaliacaoDtos.ModeraAvaliacaoRequest(StatusAvaliacao.APROVADA));
    }

    private void favoritar(Usuario cliente, Empresa empresa) {
        Favorito favorito = new Favorito();
        favorito.setUsuario(cliente);
        favorito.setEmpresa(empresa);
        favoritoRepository.save(favorito);
    }

    private Assinatura criarAssinaturaAtiva(Usuario dono, Empresa empresa, String asaasAssinaturaId) {
        empresa.setAprovada(true);
        empresa.setPremiumAtivo(true);
        empresaRepository.save(empresa);

        Assinatura assinatura = new Assinatura();
        assinatura.setUsuario(dono);
        assinatura.setEmpresa(empresa);
        assinatura.setTipo(TipoAssinatura.MENSAL);
        assinatura.setStatus(StatusAssinatura.ATIVA);
        assinatura.setAsaasAssinaturaId(asaasAssinaturaId);
        return assinaturaRepository.save(assinatura);
    }
}
