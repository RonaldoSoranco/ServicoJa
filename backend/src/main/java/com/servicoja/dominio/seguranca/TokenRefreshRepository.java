package com.servicoja.dominio.seguranca;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRefreshRepository extends JpaRepository<TokenRefresh, Long> {

    Optional<TokenRefresh> findByToken(String token);

    List<TokenRefresh> findAllByUsuarioIdAndRevogadoFalse(Long usuarioId);
}
