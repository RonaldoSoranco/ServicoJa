package com.servicoja.dominio.seguranca;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRecuperacaoRepository extends JpaRepository<TokenRecuperacao, Long> {

    Optional<TokenRecuperacao> findFirstByTokenAndUsadoFalse(String token);

    List<TokenRecuperacao> findAllByUsuarioIdAndUsadoFalse(Long usuarioId);
}
