package com.servicoja.seguranca;

import com.servicoja.dominio.usuario.Perfil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String TIPO_ACESSO = "ACESSO";
    public static final String TIPO_REFRESH = "REFRESH";
    public static final String EMISSOR = "servico-ja-api";
    public static final String AUDIENCIA = "servico-ja-app";

    private static final String SEGREDO_DESENVOLVIMENTO = "dev-segredo-servico-ja-troque-em-producao-32-bytes-min";

    private final SecretKey chave;
    private final long expiracaoAcessoMin;
    private final long expiracaoRefreshDias;

    public JwtService(
            @Value("${servico-ja.jwt.segredo}") String segredo,
            @Value("${servico-ja.jwt.expiracao-acesso-min}") long expiracaoAcessoMin,
            @Value("${servico-ja.jwt.expiracao-refresh-dias}") long expiracaoRefreshDias,
            @Value("${spring.profiles.active:}") String perfisAtivos) {
        validarSegredo(segredo, perfisAtivos);
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoAcessoMin = expiracaoAcessoMin;
        this.expiracaoRefreshDias = expiracaoRefreshDias;
    }

    private void validarSegredo(String segredo, String perfisAtivos) {
        if (segredo == null || segredo.isBlank()) {
            throw new IllegalStateException("A propriedade servico-ja.jwt.segredo (JWT_SECRET) e obrigatoria.");
        }
        if (segredo.equals(SEGREDO_DESENVOLVIMENTO) && ehProducao(perfisAtivos)) {
            throw new IllegalStateException(
                    "O segredo JWT padrao de desenvolvimento nao pode ser usado em producao. Defina JWT_SECRET.");
        }
    }

    private boolean ehProducao(String perfisAtivos) {
        return java.util.Arrays.asList(perfisAtivos.split(",")).contains("prod");
    }

    public String gerarTokenAcesso(Long usuarioId, String email, Perfil perfil) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .issuer(EMISSOR)
                .audience().add(AUDIENCIA).and()
                .subject(email)
                .claim("usuarioId", usuarioId)
                .claim("perfil", perfil.name())
                .claim("tipo", TIPO_ACESSO)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracaoAcessoMin, ChronoUnit.MINUTES)))
                .signWith(chave)
                .compact();
    }

    public String gerarTokenRefresh(Long usuarioId) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .issuer(EMISSOR)
                .audience().add(AUDIENCIA).and()
                .subject(String.valueOf(usuarioId))
                .id(UUID.randomUUID().toString())
                .claim("tipo", TIPO_REFRESH)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracaoRefreshDias, ChronoUnit.DAYS)))
                .signWith(chave)
                .compact();
    }

    public Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .requireIssuer(EMISSOR)
                .requireAudience(AUDIENCIA)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean tokenValido(String token) {
        try {
            extrairClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
