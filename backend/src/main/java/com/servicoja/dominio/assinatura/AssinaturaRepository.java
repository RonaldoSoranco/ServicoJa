package com.servicoja.dominio.assinatura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {

    Optional<Assinatura> findFirstByEmpresaIdAndStatusOrderByIdDesc(Long empresaId, StatusAssinatura status);

    Optional<Assinatura> findByAsaasAssinaturaId(String asaasAssinaturaId);

    boolean existsByEmpresaIdAndStatus(Long empresaId, StatusAssinatura status);
}
