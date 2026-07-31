package com.servicoja.dominio.pagamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByAsaasPagamentoId(String asaasPagamentoId);

    Optional<Pagamento> findByAssinaturaId(Long assinaturaId);
}
