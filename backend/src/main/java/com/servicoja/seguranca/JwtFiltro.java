package com.servicoja.seguranca;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFiltro extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFiltro(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecalho != null && cabecalho.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = cabecalho.substring(7);
            if (jwtService.tokenValido(token)) {
                Claims claims = jwtService.extrairClaims(token);
                Long usuarioId = claims.get("usuarioId", Long.class);
                String perfil = claims.get("perfil", String.class);
                String email = claims.getSubject();

                UsuarioPrincipal principal = new UsuarioPrincipal(usuarioId, email, perfil);
                var autenticacao = UsernamePasswordAuthenticationToken.authenticated(
                        principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + perfil)));
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            }
        }
        chain.doFilter(request, response);
    }
}
