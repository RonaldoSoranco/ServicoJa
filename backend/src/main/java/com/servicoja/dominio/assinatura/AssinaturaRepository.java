package com.servicoja.dominio.assinatura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {

    Optional<Assinatura> findFirstByEmpresaIdAndStatusOrderByIdDesc(Long empresaId, StatusAssinatura status);

    Optional<Assinatura> findByAsaasAssinaturaId(String asaasAssinaturaId);

    boolean existsByEmpresaIdAndStatus(Long empresaId, StatusAssinatura status);

    boolean existsByEmpresaIdAndStatusIn(Long empresaId, Collection<StatusAssinatura> statuses);

    List<Assinatura> findByEmpresaIdAndStatusIn(Long empresaId, Collection<StatusAssinatura> statuses);
}
