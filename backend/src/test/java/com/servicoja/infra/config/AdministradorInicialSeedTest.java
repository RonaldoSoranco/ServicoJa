package com.servicoja.infra.config;

import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdministradorInicialSeedTest {

    private static final String EMAIL = "admin@servicoja.com.br";
    private static final String SENHA_FORTE = "Senha-Forte-Do-Admin-2026";

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder codificador = new BCryptPasswordEncoder();

    @Test
    void naoFazNadaQuandoJaExisteAdministrador() {
        when(usuarioRepository.existsByPerfil(Perfil.ADMIN)).thenReturn(true);

        seed("prod", SENHA_FORTE).run();

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void emProducaoSemSenhaConfiguradaNaoCriaAdministrador() {
        seed("prod", "").run();

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void emProducaoRecusaSenhaCurta() {
        assertThatThrownBy(() -> seed("prod", "curta123").run())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN_SEED_SENHA");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void emProducaoCriaAdministradorComASenhaConfigurada() {
        seed("prod", SENHA_FORTE).run();

        Usuario admin = administradorSalvo();
        assertThat(admin.getPerfil()).isEqualTo(Perfil.ADMIN);
        assertThat(admin.getEmail()).isEqualTo(EMAIL);
        assertThat(codificador.matches(SENHA_FORTE, admin.getSenha())).isTrue();
    }

    @Test
    void foraDeProducaoGeraSenhaQuandoNaoConfigurada() {
        seed("dev", "").run();

        assertThat(administradorSalvo().getSenha()).startsWith("$2");
    }

    @Test
    void naoPromoveContaExistenteComOMesmoEmail() {
        when(usuarioRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(true);

        seed("prod", SENHA_FORTE).run();

        verify(usuarioRepository, never()).save(any());
    }

    private AdministradorInicialSeed seed(String perfil, String senha) {
        MockEnvironment ambiente = new MockEnvironment();
        ambiente.setActiveProfiles(perfil);
        return new AdministradorInicialSeed(usuarioRepository, codificador, ambiente, EMAIL, senha);
    }

    private Usuario administradorSalvo() {
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        return captor.getValue();
    }
}
