package com.servicoja.dominio.empresa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    List<Empresa> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByCategoriaId(Long categoriaId);

    @Query("""
            SELECT e FROM Empresa e
            WHERE e.aprovada = true
              AND (cast(:categoriaId as long) IS NULL OR e.categoria.id = cast(:categoriaId as long))
              AND (cast(:nome as string) IS NULL
                   OR lower(e.nome) LIKE lower(concat('%', cast(:nome as string), '%')))
              AND (cast(:cidade as string) IS NULL OR lower(e.cidade) = lower(cast(:cidade as string)))
              AND (cast(:uf as string) IS NULL OR upper(e.uf) = upper(cast(:uf as string)))
            ORDER BY e.destaque DESC, e.mediaAvaliacoes DESC, e.nome ASC
            """)
    Page<Empresa> buscar(
            @Param("categoriaId") Long categoriaId,
            @Param("nome") String nome,
            @Param("cidade") String cidade,
            @Param("uf") String uf,
            Pageable pageable);

    @Query("""
            SELECT e FROM Empresa e
            WHERE (cast(:categoriaId as long) IS NULL OR e.categoria.id = cast(:categoriaId as long))
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

    long countByPremiumAtivoTrue();

    long countByAprovadaFalse();
}
