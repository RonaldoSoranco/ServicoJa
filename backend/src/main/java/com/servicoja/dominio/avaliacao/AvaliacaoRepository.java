package com.servicoja.dominio.avaliacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByUsuarioIdAndEmpresaId(Long usuarioId, Long empresaId);

    boolean existsByUsuarioIdAndEmpresaId(Long usuarioId, Long empresaId);

    Page<Avaliacao> findByEmpresaIdAndStatus(Long empresaId, StatusAvaliacao status, Pageable pageable);

    List<Avaliacao> findByEmpresaIdAndStatus(Long empresaId, StatusAvaliacao status);

    Page<Avaliacao> findByStatus(StatusAvaliacao status, Pageable pageable);

    List<Avaliacao> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    long countByStatus(StatusAvaliacao status);

    @Query("SELECT COALESCE(AVG(a.nota), 0) FROM Avaliacao a WHERE a.empresa.id = :empresaId AND a.status = 'APROVADA'")
    Double mediaNotaPorEmpresa(@Param("empresaId") Long empresaId);

    long countByEmpresaIdAndStatus(Long empresaId, StatusAvaliacao status);
}
