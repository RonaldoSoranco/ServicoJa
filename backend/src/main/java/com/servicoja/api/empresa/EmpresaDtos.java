package com.servicoja.api.empresa;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class EmpresaDtos {

    private EmpresaDtos() {
    }

    public record EmpresaRequest(
            @NotBlank(message = "Informe o nome da empresa.") @Size(max = 150) String nome,
            @NotNull(message = "Informe a categoria.") Long categoriaId,
            @Size(max = 255) String descricaoCurta,
            String descricaoCompleta,
            @Size(max = 500) String logoUrl,
            @Size(max = 20) @Pattern(regexp = "^$|^[0-9+()\\s-]*$", message = "Telefone invalido.") String telefone,
            @Size(max = 20) @Pattern(regexp = "^$|^[0-9+()\\s-]*$", message = "Whatsapp invalido.") String whatsapp,
            @Size(max = 180) @jakarta.validation.constraints.Email String emailContato,
            @Size(max = 9) String cep,
            @Size(max = 255) String endereco,
            @Size(max = 10) String numero,
            @Size(max = 100) String bairro,
            @NotBlank(message = "Informe a cidade.") @Size(max = 100) String cidade,
            @NotBlank(message = "Informe o estado.")
            @Size(min = 2, max = 2) @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras.") String uf,
            @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal latitude,
            @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal longitude,
            String horarioFuncionamento,
            String redesSociais,
            @Size(max = 255) String site) {
    }

    public record FotoRequest(
            @NotBlank(message = "Informe a URL da foto.") @Size(max = 500) String url,
            @Size(max = 255) String descricao,
            Integer ordem) {
    }

    public record FotoResposta(
            Long id,
            String url,
            String descricao,
            Integer ordem) {
    }

    public record PortfolioRequest(
            @Size(max = 120) String titulo,
            String descricao,
            @Size(max = 500) String urlMidia) {
    }

    public record PortfolioResposta(
            Long id,
            String titulo,
            String descricao,
            String urlMidia) {
    }

    public record CategoriaSimplificada(
            Long id,
            String nome) {
    }

    public record EmpresaResposta(
            Long id,
            String nome,
            CategoriaSimplificada categoria,
            String nomeResponsavel,
            String descricaoCurta,
            String descricaoCompleta,
            String logoUrl,
            String telefone,
            String whatsapp,
            String emailContato,
            String cep,
            String endereco,
            String numero,
            String bairro,
            String cidade,
            String uf,
            BigDecimal latitude,
            BigDecimal longitude,
            String horarioFuncionamento,
            String redesSociais,
            String site,
            boolean premiumAtivo,
            OffsetDateTime premiumAte,
            boolean destaque,
            boolean aprovada,
            boolean perfilCompleto,
            BigDecimal mediaAvaliacoes,
            Integer totalAvaliacoes,
            List<FotoResposta> fotos,
            List<PortfolioResposta> portfolios) {
    }

    public record EmpresaSimplesResposta(
            Long id,
            String nome,
            CategoriaSimplificada categoria,
            String logoUrl,
            String cidade,
            String uf,
            boolean premiumAtivo,
            boolean destaque,
            boolean perfilCompleto,
            BigDecimal mediaAvaliacoes,
            Integer totalAvaliacoes) {
    }

    public record MensagemResposta(String mensagem) {
    }
}
