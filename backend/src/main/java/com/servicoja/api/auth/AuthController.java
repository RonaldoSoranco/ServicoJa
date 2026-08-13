package com.servicoja.api.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacao", description = "Cadastro, login, recuperacao de senha e tokens")
public class AuthController {

    private static final String CACHE_CONTROL_NO_STORE = "no-store";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastro/cliente")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.TokenResposta cadastrarCliente(@Valid @RequestBody AuthDtos.CadastroClienteRequest requisicao,
                                                   HttpServletRequest request, HttpServletResponse response) {
        bloquearCache(response);
        return authService.cadastrarCliente(requisicao, request.getRemoteAddr());
    }

    @PostMapping("/cadastro/empresa")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.TokenResposta cadastrarEmpresa(@Valid @RequestBody AuthDtos.CadastroEmpresaRequest requisicao,
                                                   HttpServletRequest request, HttpServletResponse response) {
        bloquearCache(response);
        return authService.cadastrarEmpresa(requisicao, request.getRemoteAddr());
    }

    @PostMapping("/login")
    public AuthDtos.TokenResposta login(@Valid @RequestBody AuthDtos.LoginRequest requisicao,
                                        HttpServletRequest request, HttpServletResponse response) {
        bloquearCache(response);
        return authService.login(requisicao, request.getRemoteAddr());
    }

    @PostMapping("/refresh")
    public AuthDtos.TokenResposta renovarToken(@Valid @RequestBody AuthDtos.RefreshRequest requisicao,
                                               HttpServletRequest request, HttpServletResponse response) {
        bloquearCache(response);
        return authService.renovarToken(requisicao, request.getRemoteAddr());
    }

    @PostMapping("/logout")
    public AuthDtos.MensagemResposta sair(@Valid @RequestBody AuthDtos.RefreshRequest requisicao) {
        authService.sair(requisicao.tokenRefresh());
        return new AuthDtos.MensagemResposta("Sessao encerrada com sucesso.");
    }

    @PostMapping("/recuperar-senha")
    public AuthDtos.MensagemResposta recuperarSenha(@Valid @RequestBody AuthDtos.RecuperarSenhaRequest requisicao,
                                                    HttpServletRequest request) {
        authService.recuperarSenha(requisicao, request.getRemoteAddr());
        return new AuthDtos.MensagemResposta("Se o e-mail estiver cadastrado, enviaremos as instrucoes.");
    }

    @PostMapping("/redefinir-senha")
    public AuthDtos.MensagemResposta redefinirSenha(@Valid @RequestBody AuthDtos.RedefinirSenhaRequest requisicao,
                                                    HttpServletRequest request) {
        authService.redefinirSenha(requisicao, request.getRemoteAddr());
        return new AuthDtos.MensagemResposta("Senha redefinida com sucesso.");
    }

    @PutMapping("/senha")
    public AuthDtos.MensagemResposta alterarSenha(@Valid @RequestBody AuthDtos.AlterarSenhaRequest requisicao) {
        authService.alterarSenha(requisicao);
        return new AuthDtos.MensagemResposta("Senha alterada com sucesso.");
    }

    @PutMapping("/perfil")
    public AuthDtos.UsuarioResposta atualizarPerfil(@Valid @RequestBody AuthDtos.AtualizarPerfilRequest requisicao) {
        return authService.atualizarPerfil(requisicao);
    }

    @GetMapping("/me")
    public AuthDtos.UsuarioResposta obterUsuarioAtual() {
        return authService.obterUsuarioAtual();
    }

    private void bloquearCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", CACHE_CONTROL_NO_STORE);
    }
}
