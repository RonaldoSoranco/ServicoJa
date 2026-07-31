package com.servicoja.dominio.log;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, Long> {

    Page<Log> findAllByOrderByCriadoEmDesc(Pageable pageable);
}
