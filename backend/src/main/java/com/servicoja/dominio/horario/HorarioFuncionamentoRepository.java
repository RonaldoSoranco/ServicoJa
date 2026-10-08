package com.servicoja.dominio.horario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface HorarioFuncionamentoRepository extends JpaRepository<HorarioFuncionamento, Long> {

    List<HorarioFuncionamento> findByEmpresaIdOrderByDiaSemanaAscAbreAsc(Long empresaId);

    List<HorarioFuncionamento> findByEmpresaIdIn(Collection<Long> empresaIds);

    void deleteByEmpresaId(Long empresaId);
}
