package com.servicoja.api.auth;

import com.servicoja.dominio.seguranca.TokenRecuperacaoRepository;
import com.servicoja.dominio.usuario.Perfil;
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
class AuthIntegracaoTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenRecuperacaoRepository tokenRecuperacaoRepository;

    @Test
    void cadastroClienteGeraTokensDeAcesso() {
        AuthDtos.TokenResposta resposta = authService.cadastrarCliente(
                new AuthDtos.CadastroClienteRequest(
                        "Maria Silva", "maria@teste.com", "senha12345", "(54) 99999-0001"));

        assertThat(resposta.tokenAcesso()).isNotBlank();
        assertThat(resposta.tokenRefresh()).isNotBlank();
        assertThat(resposta.usuario().perfil()).isEqualTo(Perfil.CLIENTE);
    }

    @Test
    void loginComCredenciaisValidasRetornaToken() {
        authService.cadastrarCliente(
                new AuthDtos.CadastroClienteRequest(
                        "Joao Teste", "joao@teste.com", "senha12345", null));

        AuthDtos.TokenResposta resposta = authService.login(
                new AuthDtos.LoginRequest("joao@teste.com", "senha12345"), "127.0.0.1");

        assertThat(resposta.tokenAcesso()).isNotBlank();
    }

    @Test
    void loginComSenhaInvalidaFalha() {
        authService.cadastrarCliente(
                new AuthDtos.CadastroClienteRequest(
                        "Joao Teste", "joao2@teste.com", "senha12345", null));

        assertThatThrownBy(() -> authService.login(
                new AuthDtos.LoginRequest("joao2@teste.com", "senhaErrada"), "127.0.0.1"))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Credenciais invalidas");
    }

    @Test
    void emailDuplicadoNaoPodeCadastrarDuasVezes() {
        var cadastro = new AuthDtos.CadastroClienteRequest(
                "Ana Teste", "ana@teste.com", "senha12345", null);
        authService.cadastrarCliente(cadastro);

        assertThatThrownBy(() -> authService.cadastrarCliente(cadastro))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Ja existe uma conta");
    }

    @Test
    void recuperarSenhaCriaTokenDeRecuperacao() {
        authService.cadastrarCliente(
                new AuthDtos.CadastroClienteRequest(
                        "Recupera Teste", "recupera@teste.com", "senha12345", null));

        authService.recuperarSenha(new AuthDtos.RecuperarSenhaRequest("recupera@teste.com"));

        assertThat(tokenRecuperacaoRepository.findAll())
                .anyMatch(token -> token.getUsuario().getEmail().equals("recupera@teste.com"));
    }
}
