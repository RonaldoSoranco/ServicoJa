package com.servicoja.dominio.seguranca;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRefreshRepository extends JpaRepository<TokenRefresh, Long> {

    Optional<TokenRefresh> findByTokenAndRevogadoFalse(String token);
}
