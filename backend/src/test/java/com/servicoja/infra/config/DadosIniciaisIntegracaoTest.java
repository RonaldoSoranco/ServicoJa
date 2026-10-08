package com.servicoja.infra.config;

import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DadosIniciaisIntegracaoTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void categoriasIniciaisVemDaMigracaoEExistemEmQualquerAmbiente() {
        assertThat(categoriaRepository.existsByNomeIgnoreCase("Eletricista")).isTrue();
        assertThat(categoriaRepository.existsByNomeIgnoreCase("Salão de Beleza")).isTrue();
        assertThat(categoriaRepository.findAllByAtivaTrueOrderByNomeAsc()).hasSizeGreaterThanOrEqualTo(10);
    }

    @Test
    void administradorInicialECriadoNaSubida() {
        assertThat(usuarioRepository.existsByPerfil(Perfil.ADMIN)).isTrue();
    }
}
