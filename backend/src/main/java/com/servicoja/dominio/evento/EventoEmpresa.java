package com.servicoja.dominio.evento;

import com.servicoja.dominio.empresa.Empresa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/** Registro anonimo de uma interacao com a empresa: nao guarda quem visitou, so quando. */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "eventos_empresa")
public class EventoEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEventoEmpresa tipo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    public EventoEmpresa(Empresa empresa, TipoEventoEmpresa tipo, OffsetDateTime criadoEm) {
        this.empresa = empresa;
        this.tipo = tipo;
        this.criadoEm = criadoEm;
    }
}
