package com.servicoja.dominio.assinatura;

import com.servicoja.dominio.empresa.Empresa;
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
@Table(name = "assinaturas")
public class Assinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAssinatura tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusAssinatura status = StatusAssinatura.AGUARDANDO_PAGAMENTO;

    @Column(name = "inicio_em")
    private OffsetDateTime inicioEm;

    @Column(name = "fim_em")
    private OffsetDateTime fimEm;

    @Column(name = "asaas_assinatura_id", length = 100)
    private String asaasAssinaturaId;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

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
