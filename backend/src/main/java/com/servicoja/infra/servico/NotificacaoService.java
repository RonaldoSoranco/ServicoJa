package com.servicoja.infra.servico;

import com.servicoja.dominio.notificacao.Notificacao;
import com.servicoja.dominio.notificacao.NotificacaoRepository;
import com.servicoja.dominio.notificacao.TipoNotificacao;
import com.servicoja.dominio.usuario.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @Transactional
    public void enviar(Usuario destinatario, String titulo, String mensagem, TipoNotificacao tipo) {
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(destinatario);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setTipo(tipo);
        notificacaoRepository.save(notificacao);
    }
}
