package com.servicoja.api.auth;

import com.servicoja.dominio.categoria.Categoria;
import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.seguranca.TokenRecuperacao;
import com.servicoja.dominio.seguranca.TokenRecuperacaoRepository;
import com.servicoja.dominio.seguranca.TokenRefresh;
import com.servicoja.dominio.seguranca.TokenRefreshRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.excecao.CredenciaisInvalidasException;
import com.servicoja.infra.excecao.LimiteExcedidoException;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
import com.servicoja.infra.seguranca.LimitadorRequisicoes;
import com.servicoja.infra.seguranca.TokenHash;
import com.servicoja.infra.seguranca.UsuarioAtual;
import com.servicoja.infra.servico.EmailService;
import com.servicoja.seguranca.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final String HASH_BCRYPT_DUMMY = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final CategoriaRepository categoriaRepository;
    private final TokenRefreshRepository tokenRefreshRepository;
    private final TokenRecuperacaoRepository tokenRecuperacaoRepository;
    private final PasswordEncoder codificador;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final LimitadorRequisicoes limitador;
    private final UsuarioAtual usuarioAtual;
    private final String baseUrl;
    private final String cidadePadrao;
    private final String ufPadrao;

    public AuthService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            CategoriaRepository categoriaRepository,
            TokenRefreshRepository tokenRefreshRepository,
            TokenRecuperacaoRepository tokenRecuperacaoRepository,
            PasswordEncoder codificador,
            JwtService jwtService,
            EmailService emailService,
            LimitadorRequisicoes limitador,
            UsuarioAtual usuarioAtual,
            @Value("${servico-ja.app.base-url}") String baseUrl,
            @Value("${servico-ja.app.cidade-padrao}") String cidadePadrao,
            @Value("${servico-ja.app.uf-padrao}") String ufPadrao) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.categoriaRepository = categoriaRepository;
        this.tokenRefreshRepository = tokenRefreshRepository;
        this.tokenRecuperacaoRepository = tokenRecuperacaoRepository;
        this.codificador = codificador;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.limitador = limitador;
        this.usuarioAtual = usuarioAtual;
        this.baseUrl = baseUrl;
        this.cidadePadrao = cidadePadrao;
        this.ufPadrao = ufPadrao;
    }

    @Transactional
    public AuthDtos.TokenResposta cadastrarCliente(AuthDtos.CadastroClienteRequest requisicao, String ip) {
        limitarCadastro(ip);
        verificarEmailDisponivel(requisicao.email());
        Usuario usuario = new Usuario();
        usuario.setNome(requisicao.nome().trim());
        usuario.setEmail(requisicao.email().trim().toLowerCase());
        usuario.setSenha(codificador.encode(requisicao.senha()));
        usuario.setTelefone(requisicao.telefone());
        usuario.setPerfil(Perfil.CLIENTE);
        usuarioRepository.save(usuario);
        return gerarTokens(usuario);
    }

    @Transactional
    public AuthDtos.TokenResposta cadastrarEmpresa(AuthDtos.CadastroEmpresaRequest requisicao, String ip) {
        limitarCadastro(ip);
        verificarEmailDisponivel(requisicao.email());

        Usuario usuario = new Usuario();
        usuario.setNome(requisicao.nomeResponsavel().trim());
        usuario.setEmail(requisicao.email().trim().toLowerCase());
        usuario.setSenha(codificador.encode(requisicao.senha()));
        usuario.setTelefone(requisicao.telefone());
        usuario.setCpf(requisicao.cpf());
        usuario.setPerfil(Perfil.EMPRESA);
        usuarioRepository.save(usuario);

        Empresa empresa = new Empresa();
        empresa.setUsuario(usuario);
        empresa.setNome(requisicao.nomeEmpresa().trim());
        empresa.setCidade(requisicao.cidade() != null ? requisicao.cidade() : cidadePadrao);
        empresa.setUf(requisicao.uf() != null ? requisicao.uf().toUpperCase() : ufPadrao);
        Categoria categoria = categoriaRepository.findById(requisicao.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria nao encontrada."));
        empresa.setCategoria(categoria);
        empresaRepository.save(empresa);

        return gerarTokens(usuario);
    }

    @Transactional
    public AuthDtos.TokenResposta login(AuthDtos.LoginRequest requisicao, String ip) {
        String email = requisicao.email().trim().toLowerCase();
        String chaveLimite = "login:" + ip + ":" + email;
        if (!limitador.permitido(chaveLimite, 5, Duration.ofMinutes(1))) {
            throw new LimiteExcedidoException("Muitas tentativas de login. Aguarde 1 minuto e tente novamente.");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElse(null);
        if (usuario == null) {
            codificador.matches(requisicao.senha(), HASH_BCRYPT_DUMMY);
            throw new CredenciaisInvalidasException("Credenciais invalidas.");
        }
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new CredenciaisInvalidasException("Credenciais invalidas.");
        }
        if (!codificador.matches(requisicao.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException("Credenciais invalidas.");
        }

        limitador.limpar(chaveLimite);
        return gerarTokens(usuario);
    }

    @Transactional
    public AuthDtos.TokenResposta renovarToken(AuthDtos.RefreshRequest requisicao, String ip) {
        String chaveLimite = "refresh:" + ip;
        if (!limitador.permitido(chaveLimite, 20, Duration.ofMinutes(1))) {
            throw new LimiteExcedidoException("Muitas solicitacoes de renovacao. Aguarde 1 minuto.");
        }

        if (!jwtService.tokenValido(requisicao.tokenRefresh())) {
            throw new NegocioException("Token de atualizacao invalido ou revogado.");
        }
        Claims claims = jwtService.extrairClaims(requisicao.tokenRefresh());
        if (!JwtService.TIPO_REFRESH.equals(claims.get("tipo", String.class))) {
            throw new NegocioException("Token de atualizacao invalido ou revogado.");
        }

        String hash = TokenHash.de(requisicao.tokenRefresh());
        TokenRefresh token = tokenRefreshRepository.findByToken(hash)
                .orElseThrow(() -> new NegocioException("Token de atualizacao invalido ou revogado."));

        if (Boolean.TRUE.equals(token.getRevogado())) {
            revogarTodosRefreshTokens(token.getUsuario());
            throw new NegocioException("Token de atualizacao invalido ou revogado.");
        }
        if (token.getExpiraEm().isBefore(OffsetDateTime.now())) {
            throw new NegocioException("Token de atualizacao expirado.");
        }

        token.setRevogado(true);
        tokenRefreshRepository.save(token);

        Usuario usuario = token.getUsuario();
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new CredenciaisInvalidasException("Credenciais invalidas.");
        }
        return gerarTokens(usuario);
    }

    @Transactional
    public void sair(String tokenRefresh) {
        tokenRefreshRepository.findByToken(TokenHash.de(tokenRefresh))
                .ifPresent(token -> {
                    token.setRevogado(true);
                    tokenRefreshRepository.save(token);
                });
    }

    @Transactional
    public void recuperarSenha(AuthDtos.RecuperarSenhaRequest requisicao, String ip) {
        String chaveLimite = "recuperar-senha:" + ip;
        if (!limitador.permitido(chaveLimite, 3, Duration.ofMinutes(15))) {
            throw new LimiteExcedidoException("Muitas solicitacoes. Aguarde 15 minutos e tente novamente.");
        }

        usuarioRepository.findByEmailIgnoreCase(requisicao.email().trim())
                .ifPresent(usuario -> {
                    String token = UUID.randomUUID().toString().replace("-", "") + gerarSufixoAleatorio();
                    revogarTokensRecuperacaoPendentes(usuario);
                    TokenRecuperacao registro = new TokenRecuperacao(usuario, TokenHash.de(token), OffsetDateTime.now().plusHours(2));
                    tokenRecuperacaoRepository.save(registro);
                    String link = baseUrl + "/api/auth/redefinir-senha?token=" + token;
                    emailService.enviar(
                            usuario.getEmail(),
                            "Recuperacao de senha - Servico Ja",
                            "Para redefinir sua senha, utilize o token: " + token
                                    + "\n\nLink: " + link
                                    + "\n\nO token expira em 2 horas.");
                });
    }

    @Transactional
    public void redefinirSenha(AuthDtos.RedefinirSenhaRequest requisicao, String ip) {
        String chaveLimite = "redefinir-senha:" + ip;
        if (!limitador.permitido(chaveLimite, 5, Duration.ofMinutes(15))) {
            throw new LimiteExcedidoException("Muitas tentativas. Aguarde 15 minutos e tente novamente.");
        }

        TokenRecuperacao registro = tokenRecuperacaoRepository
                .findFirstByTokenAndUsadoFalse(TokenHash.de(requisicao.token()))
                .orElseThrow(() -> new NegocioException("Token de recuperacao invalido ou ja utilizado."));

        if (registro.getExpiraEm().isBefore(OffsetDateTime.now())) {
            throw new NegocioException("Token de recuperacao expirado.");
        }

        registro.setUsado(true);
        tokenRecuperacaoRepository.save(registro);

        Usuario usuario = registro.getUsuario();
        usuario.setSenha(codificador.encode(requisicao.novaSenha()));
        usuarioRepository.save(usuario);
        revogarTodosRefreshTokens(usuario);
    }

    @Transactional
    public void alterarSenha(AuthDtos.AlterarSenhaRequest requisicao) {
        Usuario usuario = usuarioAtual.obter();
        if (!codificador.matches(requisicao.senhaAtual(), usuario.getSenha())) {
            throw new NegocioException("Senha atual incorreta.");
        }
        if (requisicao.senhaAtual().equals(requisicao.novaSenha())) {
            throw new NegocioException("A nova senha deve ser diferente da senha atual.");
        }
        usuario.setSenha(codificador.encode(requisicao.novaSenha()));
        usuarioRepository.save(usuario);
        revogarTodosRefreshTokens(usuario);
    }

    @Transactional
    public AuthDtos.UsuarioResposta atualizarPerfil(AuthDtos.AtualizarPerfilRequest requisicao) {
        Usuario usuario = usuarioAtual.obter();
        usuario.setNome(requisicao.nome().trim());
        usuario.setTelefone(requisicao.telefone());
        usuarioRepository.save(usuario);
        return converter(usuario);
    }

    public AuthDtos.UsuarioResposta obterUsuarioAtual() {
        return converter(usuarioAtual.obter());
    }

    private void verificarEmailDisponivel(String email) {
        if (usuarioRepository.existsByEmailIgnoreCase(email.trim())) {
            throw new NegocioException("Ja existe uma conta com este e-mail.");
        }
    }

    private void limitarCadastro(String ip) {
        if (!limitador.permitido("cadastro:" + ip, 5, Duration.ofMinutes(10))) {
            throw new LimiteExcedidoException("Muitos cadastros neste intervalo. Aguarde 10 minutos.");
        }
    }

    private void revogarTodosRefreshTokens(Usuario usuario) {
        tokenRefreshRepository.findAllByUsuarioIdAndRevogadoFalse(usuario.getId())
                .forEach(token -> {
                    token.setRevogado(true);
                    tokenRefreshRepository.save(token);
                });
    }

    private void revogarTokensRecuperacaoPendentes(Usuario usuario) {
        tokenRecuperacaoRepository.findAllByUsuarioIdAndUsadoFalse(usuario.getId())
                .forEach(token -> {
                    token.setUsado(true);
                    tokenRecuperacaoRepository.save(token);
                });
    }

    private AuthDtos.TokenResposta gerarTokens(Usuario usuario) {
        String tokenAcesso = jwtService.gerarTokenAcesso(usuario.getId(), usuario.getEmail(), usuario.getPerfil());
        String tokenRefresh = jwtService.gerarTokenRefresh(usuario.getId());

        Claims claimsRefresh = jwtService.extrairClaims(tokenRefresh);
        OffsetDateTime expiraEm = claimsRefresh.getExpiration().toInstant()
                .atOffset(OffsetDateTime.now().getOffset());
        TokenRefresh registro = new TokenRefresh(usuario, TokenHash.de(tokenRefresh), expiraEm);
        tokenRefreshRepository.save(registro);

        return new AuthDtos.TokenResposta(
                tokenAcesso,
                tokenRefresh,
                expiraEm,
                converter(usuario));
    }

    private AuthDtos.UsuarioResposta converter(Usuario usuario) {
        return new AuthDtos.UsuarioResposta(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getPerfil());
    }

    private String gerarSufixoAleatorio() {
        byte[] bytes = new byte[6];
        ALEATORIO.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
