package com.servicoja.dominio.categoria;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(length = 100)
    private String icone;

    @Column(nullable = false)
    private Boolean ativa = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        criadoEm = OffsetDateTime.now();
    }
}
