package com.servicoja.infra.config;

import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Garante que exista um administrador para moderar empresas e avaliacoes. Roda em todos os
 * perfis, mas so cria o usuario enquanto nao houver nenhum ADMIN no banco.
 *
 * <p>Em producao a senha precisa vir de ADMIN_SEED_SENHA e nunca e gerada nem escrita no log;
 * fora de producao, se ela nao for informada, uma senha aleatoria e gerada e exibida no log.
 */
@Component
public class AdministradorInicialSeed implements CommandLineRunner {

    static final int TAMANHO_MINIMO_SENHA_PRODUCAO = 12;
    /** Limite do BCrypt: bytes alem do 72o sao ignorados/recusados. */
    static final int TAMANHO_MAXIMO_SENHA_BYTES = 72;

    private static final Logger LOGGER = LoggerFactory.getLogger(AdministradorInicialSeed.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificador;
    private final Environment ambiente;
    private final String email;
    private final String senhaConfigurada;

    public AdministradorInicialSeed(
            UsuarioRepository usuarioRepository,
            PasswordEncoder codificador,
            Environment ambiente,
            @Value("${servico-ja.admin-seed.email}") String email,
            @Value("${servico-ja.admin-seed.senha}") String senhaConfigurada) {
        this.usuarioRepository = usuarioRepository;
        this.codificador = codificador;
        this.ambiente = ambiente;
        this.email = email;
        this.senhaConfigurada = senhaConfigurada;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.existsByPerfil(Perfil.ADMIN)) {
            return;
        }
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            LOGGER.warn("Nenhum administrador foi criado: o e-mail {} ja pertence a uma conta que nao e ADMIN. "
                    + "Defina outro ADMIN_SEED_EMAIL.", email);
            return;
        }

        boolean producao = ambiente.matchesProfiles("prod");
        boolean senhaInformada = senhaConfigurada != null && !senhaConfigurada.isBlank();
        if (!senhaInformada && producao) {
            LOGGER.warn("Nenhum administrador cadastrado. Defina ADMIN_SEED_SENHA (minimo de {} caracteres) "
                    + "e reinicie a aplicacao para cria-lo.", TAMANHO_MINIMO_SENHA_PRODUCAO);
            return;
        }
        if (senhaInformada) {
            validarSenha(producao);
        }

        String senha = senhaInformada ? senhaConfigurada : gerarSenhaAleatoria();
        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setEmail(email.trim().toLowerCase());
        admin.setSenha(codificador.encode(senha));
        admin.setPerfil(Perfil.ADMIN);
        usuarioRepository.save(admin);

        if (senhaInformada) {
            LOGGER.info("Administrador inicial criado ({}).", email);
        } else {
            LOGGER.warn("Administrador inicial criado ({}) com senha gerada automaticamente: {} "
                    + "- defina ADMIN_SEED_SENHA para controlar essa senha e altere-a apos o primeiro login.",
                    email, senha);
        }
    }

    private void validarSenha(boolean producao) {
        if (producao && senhaConfigurada.length() < TAMANHO_MINIMO_SENHA_PRODUCAO) {
            throw new IllegalStateException("ADMIN_SEED_SENHA deve ter pelo menos "
                    + TAMANHO_MINIMO_SENHA_PRODUCAO + " caracteres em producao.");
        }
        if (senhaConfigurada.getBytes(StandardCharsets.UTF_8).length > TAMANHO_MAXIMO_SENHA_BYTES) {
            throw new IllegalStateException(
                    "ADMIN_SEED_SENHA deve ter no maximo " + TAMANHO_MAXIMO_SENHA_BYTES + " bytes.");
        }
    }

    private String gerarSenhaAleatoria() {
        byte[] bytes = new byte[12];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
