package com.servicoja.infra.seguranca;

import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.excecao.NaoAutenticadoException;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
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

    public Usuario obter() {
        return usuarioRepository.findById(obterId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
    }
}
