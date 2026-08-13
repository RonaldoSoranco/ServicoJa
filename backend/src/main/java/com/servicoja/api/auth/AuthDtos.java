package com.servicoja.api.auth;

import com.servicoja.dominio.usuario.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record CadastroClienteRequest(
            @NotBlank(message = "Informe o nome.") @Size(max = 120) String nome,
            @NotBlank(message = "Informe o e-mail.") @Email @Size(max = 180) String email,
            @NotBlank(message = "Informe a senha.")
            @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.") String senha,
            @Size(max = 20) @Pattern(regexp = "^$|^[0-9+()\\s-]*$", message = "Telefone invalido.") String telefone) {
    }

    public record CadastroEmpresaRequest(
            @NotBlank(message = "Informe o nome do responsavel.") @Size(max = 120) String nomeResponsavel,
            @NotBlank(message = "Informe o e-mail.") @Email @Size(max = 180) String email,
            @NotBlank(message = "Informe a senha.")
            @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.") String senha,
            @Size(max = 20) @Pattern(regexp = "^$|^[0-9+()\\s-]*$", message = "Telefone invalido.") String telefone,
            @NotBlank(message = "Informe o nome da empresa.") @Size(max = 150) String nomeEmpresa,
            @NotNull(message = "Informe a categoria.") Long categoriaId,
            @NotBlank(message = "Informe a cidade.") @Size(max = 100) String cidade,
            @NotBlank(message = "Informe o estado.")
            @Size(min = 2, max = 2) @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras.") String uf,
            @Size(max = 14) @Pattern(regexp = "^$|^[0-9]{11}$", message = "CPF invalido.") String cpf) {
    }

    public record LoginRequest(
            @NotBlank(message = "Informe o e-mail.") @Email String email,
            @NotBlank(message = "Informe a senha.") @Size(max = 72) String senha) {
    }

    public record RefreshRequest(
            @NotBlank(message = "Informe o token de atualizacao.") @Size(max = 400) String tokenRefresh) {
    }

    public record RecuperarSenhaRequest(
            @NotBlank(message = "Informe o e-mail.") @Email String email) {
    }

    public record RedefinirSenhaRequest(
            @NotBlank(message = "Informe o token.") @Size(max = 400) String token,
            @NotBlank(message = "Informe a nova senha.")
            @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.") String novaSenha) {
    }

    public record AlterarSenhaRequest(
            @NotBlank(message = "Informe a senha atual.") @Size(max = 72) String senhaAtual,
            @NotBlank(message = "Informe a nova senha.")
            @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.") String novaSenha) {
    }

    public record AtualizarPerfilRequest(
            @NotBlank(message = "Informe o nome.") @Size(max = 120) String nome,
            @Size(max = 20) @Pattern(regexp = "^$|^[0-9+()\\s-]*$", message = "Telefone invalido.") String telefone) {
    }

    public record UsuarioResposta(
            Long id,
            String nome,
            String email,
            String telefone,
            Perfil perfil) {
    }

    public record TokenResposta(
            String tokenAcesso,
            String tokenRefresh,
            OffsetDateTime expiraEm,
            UsuarioResposta usuario) {
    }

    public record MensagemResposta(String mensagem) {
    }
}
