package com.servicoja.dominio.seguranca;

import com.servicoja.dominio.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tokens_recuperacao")
public class TokenRecuperacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expira_em", nullable = false)
    private OffsetDateTime expiraEm;

    @Column(nullable = false)
    private Boolean usado = false;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    public TokenRecuperacao(Usuario usuario, String token, OffsetDateTime expiraEm) {
        this.usuario = usuario;
        this.token = token;
        this.expiraEm = expiraEm;
        this.criadoEm = OffsetDateTime.now();
    }
}
