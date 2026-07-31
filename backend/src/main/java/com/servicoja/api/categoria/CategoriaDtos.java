package com.servicoja.api.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CategoriaDtos {

    private CategoriaDtos() {
    }

    public record CategoriaRequest(
            @NotBlank(message = "Informe o nome da categoria.") @Size(max = 80) String nome,
            String descricao,
            @Size(max = 100) String icone) {
    }

    public record CategoriaResposta(
            Long id,
            String nome,
            String descricao,
            String icone,
            boolean ativa) {
    }
}
