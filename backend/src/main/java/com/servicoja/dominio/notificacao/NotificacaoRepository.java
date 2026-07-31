package com.servicoja.dominio.notificacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    Page<Notificacao> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId, Pageable pageable);

    List<Notificacao> findByUsuarioIdAndLidaFalse(Long usuarioId);

    long countByUsuarioIdAndLidaFalse(Long usuarioId);
}
