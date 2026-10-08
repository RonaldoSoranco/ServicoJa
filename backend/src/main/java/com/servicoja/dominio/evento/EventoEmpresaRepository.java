package com.servicoja.dominio.evento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface EventoEmpresaRepository extends JpaRepository<EventoEmpresa, Long> {

    interface ContagemPorTipo {
        TipoEventoEmpresa getTipo();

        Long getTotal();
    }

    @Query("""
            SELECT e.tipo AS tipo, COUNT(e) AS total
            FROM EventoEmpresa e
            WHERE e.empresa.id = :empresaId AND e.criadoEm >= :desde AND e.criadoEm < :ate
            GROUP BY e.tipo
            """)
    List<ContagemPorTipo> contarPorTipo(
            @Param("empresaId") Long empresaId,
            @Param("desde") OffsetDateTime desde,
            @Param("ate") OffsetDateTime ate);

    void deleteByEmpresaId(Long empresaId);
}
