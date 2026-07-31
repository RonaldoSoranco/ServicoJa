package com.servicoja.dominio.log;

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
@Table(name = "logs")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String acao;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @Column(length = 45)
    private String ip;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    public Log(Usuario usuario, String acao, String detalhes, String ip) {
        this.usuario = usuario;
        this.acao = acao;
        this.detalhes = detalhes;
        this.ip = ip;
        this.criadoEm = OffsetDateTime.now();
    }
}