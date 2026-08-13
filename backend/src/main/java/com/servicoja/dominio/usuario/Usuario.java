package com.servicoja.dominio.usuario;

import com.servicoja.dominio.empresa.Empresa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(length = 20)
    private String telefone;

    @Column(length = 14)
    private String cpf;

    @Column(name = "asaas_cliente_id", length = 100)
    private String asaasClienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @OneToMany(mappedBy = "usuario")
    private List<Empresa> empresas = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        OffsetDateTime agora = OffsetDateTime.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    public void preUpdate() {
        atualizadoEm = OffsetDateTime.now();
    }
}
