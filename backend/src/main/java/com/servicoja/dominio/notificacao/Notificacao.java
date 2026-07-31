package com.servicoja.dominio.notificacao;

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
@Table(name = "notificacoes")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(nullable = false)
    private Boolean lida = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoNotificacao tipo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        criadoEm = OffsetDateTime.now();
    }
}
