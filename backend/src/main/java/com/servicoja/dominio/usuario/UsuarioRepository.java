package com.servicoja.dominio.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPerfil(Perfil perfil);

    List<Usuario> findByPerfil(Perfil perfil);

    Page<Usuario> findAllByExcluidoEmIsNull(Pageable pageable);

    long countByExcluidoEmIsNull();

    long countByPerfilAndExcluidoEmIsNull(Perfil perfil);
}
