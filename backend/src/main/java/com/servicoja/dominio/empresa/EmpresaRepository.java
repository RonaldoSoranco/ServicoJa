package com.servicoja.dominio.empresa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Empresas excluidas continuam no banco apenas como historico financeiro; por isso as consultas
 * da aplicacao usam as variantes que ignoram registros com {@code excluidaEm} preenchido.
 */
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByIdAndExcluidaEmIsNull(Long id);

    List<Empresa> findByUsuarioIdAndExcluidaEmIsNull(Long usuarioId);

    boolean existsByCategoriaId(Long categoriaId);

    /**
     * Busca publica. Com {@code somenteAbertas}, so traz empresas com um horario cobrindo
     * {@code diaSemana}/{@code hora}. Com {@code porDistancia}, ordena pela distancia aproximada ate
     * ({@code latitude}, {@code longitude}) e deixa por ultimo quem nao informou a localizacao;
     * {@code fatorLongitude} vem de {@link com.servicoja.infra.Geolocalizacao#fatorLongitude}.
     */
    @Query("""
            SELECT e FROM Empresa e
            WHERE e.aprovada = true
              AND e.excluidaEm IS NULL
              AND (:categoriaId IS NULL OR e.categoria.id = :categoriaId)
              AND (cast(:nome as string) IS NULL
                   OR lower(e.nome) LIKE lower(concat('%', cast(:nome as string), '%')))
              AND (cast(:cidade as string) IS NULL OR lower(e.cidade) = lower(cast(:cidade as string)))
              AND (cast(:uf as string) IS NULL OR upper(e.uf) = upper(cast(:uf as string)))
              AND (:somenteAbertas = false OR EXISTS (
                    SELECT h.id FROM HorarioFuncionamento h
                    WHERE h.empresa = e AND h.diaSemana = :diaSemana AND h.abre <= :hora AND h.fecha > :hora))
            ORDER BY
              CASE WHEN :porDistancia = true AND e.latitude IS NULL THEN 1 ELSE 0 END,
              CASE WHEN :porDistancia = true
                   THEN (e.latitude - :latitude) * (e.latitude - :latitude)
                        + (e.longitude - :longitude) * (e.longitude - :longitude) * :fatorLongitude
                   ELSE 0 END,
              e.destaque DESC, e.mediaAvaliacoes DESC, e.nome ASC
            """)
    Page<Empresa> buscar(
            @Param("categoriaId") Long categoriaId,
            @Param("nome") String nome,
            @Param("cidade") String cidade,
            @Param("uf") String uf,
            @Param("somenteAbertas") boolean somenteAbertas,
            @Param("diaSemana") int diaSemana,
            @Param("hora") LocalTime hora,
            @Param("porDistancia") boolean porDistancia,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("fatorLongitude") BigDecimal fatorLongitude,
            Pageable pageable);

    @Query("""
            SELECT e FROM Empresa e
            WHERE e.excluidaEm IS NULL
              AND (:categoriaId IS NULL OR e.categoria.id = :categoriaId)
              AND (cast(:nome as string) IS NULL
                   OR lower(e.nome) LIKE lower(concat('%', cast(:nome as string), '%')))
              AND (cast(:cidade as string) IS NULL OR lower(e.cidade) = lower(cast(:cidade as string)))
              AND (cast(:uf as string) IS NULL OR upper(e.uf) = upper(cast(:uf as string)))
            ORDER BY e.criadoEm DESC
            """)
    Page<Empresa> buscarAdministrativo(
            @Param("categoriaId") Long categoriaId,
            @Param("nome") String nome,
            @Param("cidade") String cidade,
            @Param("uf") String uf,
            Pageable pageable);

    long countByExcluidaEmIsNull();

    long countByPremiumAtivoTrue();

    long countByAprovadaFalseAndExcluidaEmIsNull();
}
