package com.servicoja.api.avaliacao;

import com.servicoja.dominio.avaliacao.StatusAvaliacao;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public final class AvaliacaoDtos {

    private AvaliacaoDtos() {
    }

    public record AvaliacaoRequest(
            @NotNull(message = "Informe a nota.") @Min(value = 1, message = "A nota minima e 1.")
            @Max(value = 5, message = "A nota maxima e 5.") Integer nota,
            @Size(max = 2000, message = "O comentario deve ter no maximo 2000 caracteres.") String comentario) {
    }

    public record AvaliacaoResposta(
            Long id,
            Long usuarioId,
            String nomeUsuario,
            Integer nota,
            String comentario,
            StatusAvaliacao status,
            OffsetDateTime criadoEm) {
    }

    public record ModeraAvaliacaoRequest(
            @NotNull(message = "Informe o novo status.") StatusAvaliacao status) {
    }

    public record MensagemResposta(String mensagem) {
    }
}
