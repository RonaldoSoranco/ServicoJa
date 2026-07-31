package com.servicoja.dominio.favorito;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    Optional<Favorito> findByUsuarioIdAndEmpresaId(Long usuarioId, Long empresaId);

    boolean existsByUsuarioIdAndEmpresaId(Long usuarioId, Long empresaId);

    Page<Favorito> findByUsuarioId(Long usuarioId, Pageable pageable);
}
