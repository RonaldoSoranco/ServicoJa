package com.servicoja.dominio.categoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByAtivaTrueOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
