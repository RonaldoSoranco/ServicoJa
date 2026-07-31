package com.servicoja.api.notificacao;

import com.servicoja.dominio.notificacao.Notificacao;
import com.servicoja.dominio.notificacao.NotificacaoRepository;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.infra.PageResposta;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoServiceUsuario {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoServiceUsuario(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @Transactional(readOnly = true)
    public PageResposta<NotificacaoDtos.NotificacaoResposta> listar(Usuario usuario, int pagina, int tamanho) {
        return PageResposta.de(notificacaoRepository
                .findByUsuarioIdOrderByCriadoEmDesc(usuario.getId(),
                        PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), 50)))
                .map(this::converter));
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(Usuario usuario) {
        return notificacaoRepository.countByUsuarioIdAndLidaFalse(usuario.getId());
    }

    @Transactional
    public NotificacaoDtos.NotificacaoResposta marcarLida(Usuario usuario, Long id) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificacao nao encontrada."));
        if (!notificacao.getUsuario().getId().equals(usuario.getId())) {
            throw new NegocioException("Voce nao pode acessar esta notificacao.");
        }
        notificacao.setLida(true);
        return converter(notificacaoRepository.save(notificacao));
    }

    @Transactional
    public void marcarTodasLidas(Usuario usuario) {
        notificacaoRepository.findByUsuarioIdAndLidaFalse(usuario.getId())
                .forEach(notificacao -> notificacao.setLida(true));
    }

    private NotificacaoDtos.NotificacaoResposta converter(Notificacao notificacao) {
        return new NotificacaoDtos.NotificacaoResposta(
                notificacao.getId(),
                notificacao.getTitulo(),
                notificacao.getMensagem(),
                Boolean.TRUE.equals(notificacao.getLida()),
                notificacao.getTipo(),
                notificacao.getCriadoEm());
    }
}
