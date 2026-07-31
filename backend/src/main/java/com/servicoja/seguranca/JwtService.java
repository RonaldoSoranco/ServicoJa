package com.servicoja.seguranca;

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

    private final SecretKey chave;
    private final long expiracaoAcessoMin;
    private final long expiracaoRefreshDias;

    public JwtService(
            @Value("${servico-ja.jwt.segredo}") String segredo,
            @Value("${servico-ja.jwt.expiracao-acesso-min}") long expiracaoAcessoMin,
            @Value("${servico-ja.jwt.expiracao-refresh-dias}") long expiracaoRefreshDias) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoAcessoMin = expiracaoAcessoMin;
        this.expiracaoRefreshDias = expiracaoRefreshDias;
    }

    public String gerarTokenAcesso(Long usuarioId, String email, String perfil) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("usuarioId", usuarioId)
                .claim("perfil", perfil)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracaoAcessoMin, ChronoUnit.MINUTES)))
                .signWith(chave)
                .compact();
    }

    public String gerarTokenRefresh(Long usuarioId) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracaoRefreshDias, ChronoUnit.DAYS)))
                .signWith(chave)
                .compact();
    }

    public Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
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
