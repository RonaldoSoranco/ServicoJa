package com.servicoja.dominio.pagamento;

import com.servicoja.dominio.assinatura.Assinatura;
import com.servicoja.dominio.empresa.Empresa;
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
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assinatura_id")
    private Assinatura assinatura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "asaas_pagamento_id", length = 100)
    private String asaasPagamentoId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(name = "metodo_pagamento", length = 30)
    private String metodoPagamento;

    @Column(name = "link_pagamento", length = 500)
    private String linkPagamento;

    @Column(name = "pago_em")
    private OffsetDateTime pagoEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        criadoEm = OffsetDateTime.now();
    }
}
