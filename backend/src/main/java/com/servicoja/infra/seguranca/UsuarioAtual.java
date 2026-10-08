package com.servicoja.infra.seguranca;

import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.excecao.NaoAutenticadoException;
import com.servicoja.seguranca.UsuarioPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAtual {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAtual(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Long obterId() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new NaoAutenticadoException("Autenticacao necessaria.");
        }
        return principal.id();
    }

    /**
     * Carrega o usuario autenticado. Um token de acesso continua valido ate expirar, entao a conta
     * e conferida aqui: se ela foi desativada ou excluida, a requisicao e recusada na hora.
     */
    public Usuario obter() {
        Usuario usuario = usuarioRepository.findById(obterId())
                .orElseThrow(() -> new NaoAutenticadoException("Autenticacao necessaria."));
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new NaoAutenticadoException("Autenticacao necessaria.");
        }
        return usuario;
    }
}
