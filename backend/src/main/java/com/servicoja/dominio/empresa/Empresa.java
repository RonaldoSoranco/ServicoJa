package com.servicoja.dominio.empresa;

import com.servicoja.dominio.categoria.Categoria;
import com.servicoja.dominio.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 255)
    private String descricaoCurta;

    @Column(columnDefinition = "TEXT")
    private String descricaoCompleta;

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 20)
    private String telefone;

    @Column(length = 20)
    private String whatsapp;

    @Column(length = 180)
    private String emailContato;

    @Column(length = 9)
    private String cep;

    @Column(length = 255)
    private String endereco;

    @Column(length = 10)
    private String numero;

    @Column(length = 100)
    private String bairro;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(columnDefinition = "TEXT")
    private String horarioFuncionamento;

    @Column(columnDefinition = "TEXT")
    private String redesSociais;

    @Column(length = 255)
    private String site;

    @Column(name = "premium_ativo", nullable = false)
    private Boolean premiumAtivo = false;

    @Column(name = "premium_ate")
    private OffsetDateTime premiumAte;

    @Column(nullable = false)
    private Boolean destaque = false;

    @Column(nullable = false)
    private Boolean aprovada = false;

    @Column(name = "media_avaliacoes", nullable = false, precision = 2, scale = 1)
    private BigDecimal mediaAvaliacoes = BigDecimal.ZERO;

    @Column(name = "total_avaliacoes", nullable = false)
    private Integer totalAvaliacoes = 0;

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

    public boolean isPerfilCompleto() {
        return Boolean.TRUE.equals(premiumAtivo);
    }
}
