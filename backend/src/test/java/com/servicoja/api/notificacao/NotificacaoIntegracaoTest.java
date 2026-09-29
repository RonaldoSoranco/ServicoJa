package com.servicoja.api.notificacao;

import com.servicoja.dominio.notificacao.TipoNotificacao;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
import com.servicoja.infra.servico.NotificacaoService;
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
class NotificacaoIntegracaoTest {

    @Autowired
    private NotificacaoServiceUsuario notificacaoServiceUsuario;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario destinatario;
    private Usuario outroUsuario;

    @BeforeEach
    void preparar() {
        destinatario = criarUsuario("Destinatario Notificacao", "destinatario.notificacao@teste.com");
        outroUsuario = criarUsuario("Outro Usuario Notificacao", "outro.notificacao@teste.com");
    }

    @Test
    void contarNaoLidasReflideNotificacoesEnviadas() {
        notificacaoService.enviar(destinatario, "Titulo 1", "Mensagem 1", TipoNotificacao.MODERACAO);
        notificacaoService.enviar(destinatario, "Titulo 2", "Mensagem 2", TipoNotificacao.MODERACAO);

        assertThat(notificacaoServiceUsuario.contarNaoLidas(destinatario)).isEqualTo(2);
    }

    @Test
    void marcarLidaAtualizaContagemDeNaoLidas() {
        notificacaoService.enviar(destinatario, "Titulo", "Mensagem", TipoNotificacao.MODERACAO);
        Long id = notificacaoServiceUsuario.listar(destinatario, 0, 10).conteudo().get(0).id();

        notificacaoServiceUsuario.marcarLida(destinatario, id);

        assertThat(notificacaoServiceUsuario.contarNaoLidas(destinatario)).isZero();
    }

    @Test
    void marcarTodasLidasZeraContagem() {
        notificacaoService.enviar(destinatario, "Titulo 1", "Mensagem 1", TipoNotificacao.MODERACAO);
        notificacaoService.enviar(destinatario, "Titulo 2", "Mensagem 2", TipoNotificacao.MODERACAO);

        notificacaoServiceUsuario.marcarTodasLidas(destinatario);

        assertThat(notificacaoServiceUsuario.contarNaoLidas(destinatario)).isZero();
    }

    @Test
    void naoPodeMarcarComoLidaNotificacaoDeOutroUsuario() {
        notificacaoService.enviar(destinatario, "Titulo", "Mensagem", TipoNotificacao.MODERACAO);
        Long id = notificacaoServiceUsuario.listar(destinatario, 0, 10).conteudo().get(0).id();

        assertThatThrownBy(() -> notificacaoServiceUsuario.marcarLida(outroUsuario, id))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("nao pode acessar esta notificacao");
    }

    @Test
    void marcarLidaComIdInexistenteFalha() {
        assertThatThrownBy(() -> notificacaoServiceUsuario.marcarLida(destinatario, -1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private Usuario criarUsuario(String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha("$2a$10$teste");
        usuario.setPerfil(Perfil.CLIENTE);
        return usuarioRepository.save(usuario);
    }
}
