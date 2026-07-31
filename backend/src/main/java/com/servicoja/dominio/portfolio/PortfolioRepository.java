package com.servicoja.dominio.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByEmpresaIdOrderByCriadoEmDesc(Long empresaId);
}
