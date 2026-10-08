package com.servicoja.api.empresa;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.servicoja.dominio.evento.TipoEventoEmpresa;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

public final class EmpresaDtos {

    private EmpresaDtos() {
    }

    /**
     * Dados editaveis da empresa. O logo nao faz parte deste corpo: ele so pode ser trocado pelo
     * upload em {@code POST /api/empresas/{id}/logo}, que grava o arquivo na pasta da empresa.
     * {@code horarios} nulo mantem os horarios atuais; lista vazia remove todos.
     */
    public record EmpresaRequest(
            @NotBlank(message = "Informe o nome da empresa.") @Size(max = 150) String nome,
            @NotNull(message = "Informe a categoria.") Long categoriaId,
            @Size(max = 255) String descricaoCurta,
            String descricaoCompleta,
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
            @Size(max = 21, message = "Informe no maximo 3 intervalos por dia.") List<@Valid HorarioDto> horarios,
            String redesSociais,
            @Size(max = 255) String site) {
    }

    /** Intervalo de atendimento em um dia da semana (1 = segunda ... 7 = domingo), no formato "HH:mm". */
    public record HorarioDto(
            @NotNull(message = "Informe o dia da semana.")
            @Min(value = 1, message = "Dia da semana invalido.")
            @Max(value = 7, message = "Dia da semana invalido.") Integer diaSemana,
            @NotNull(message = "Informe o horario de abertura.") @JsonFormat(pattern = "HH:mm") LocalTime abre,
            @NotNull(message = "Informe o horario de fechamento.") @JsonFormat(pattern = "HH:mm") LocalTime fecha) {
    }

    /**
     * Filtros da busca publica. Com {@code latitude}/{@code longitude} (posicao de quem busca),
     * os resultados vem ordenados por proximidade e com a distancia de cada empresa.
     */
    public record FiltroBusca(
            Long categoriaId,
            String nome,
            String cidade,
            String uf,
            Double latitude,
            Double longitude,
            boolean somenteAbertas) {

        public FiltroBusca {
            if ((latitude == null) != (longitude == null)) {
                throw new IllegalArgumentException("Informe latitude e longitude juntas.");
            }
            if (latitude != null && (Math.abs(latitude) > 90 || Math.abs(longitude) > 180)) {
                throw new IllegalArgumentException("Coordenadas invalidas.");
            }
        }

        public boolean temLocalizacao() {
            return latitude != null;
        }
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
            String nome,
            String icone) {
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
            List<HorarioDto> horarios,
            boolean abertoAgora,
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
            BigDecimal latitude,
            BigDecimal longitude,
            Double distanciaKm,
            boolean abertoAgora,
            boolean temHorarios,
            boolean premiumAtivo,
            boolean destaque,
            boolean perfilCompleto,
            BigDecimal mediaAvaliacoes,
            Integer totalAvaliacoes) {
    }

    public record EventoRequest(@NotNull(message = "Informe o tipo do evento.") TipoEventoEmpresa tipo) {
    }

    /** Total no periodo atual e no periodo anterior de mesmo tamanho, para mostrar a tendencia. */
    public record MetricaResposta(long atual, long anterior) {
    }

    public record DesempenhoResposta(
            int dias,
            MetricaResposta visualizacoes,
            MetricaResposta cliquesWhatsapp,
            MetricaResposta cliquesLigar,
            MetricaResposta cliquesMapa) {
    }

    public record MensagemResposta(String mensagem) {
    }
}
