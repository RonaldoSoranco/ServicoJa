package com.servicoja.infra.armazenamento;

import com.servicoja.infra.excecao.NegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ArmazenamentoLocalService implements ArmazenamentoArquivos {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArmazenamentoLocalService.class);
    private static final String PREFIXO_PUBLICO = "/uploads/";
    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "webp");

    private final Path diretorioBase;

    public ArmazenamentoLocalService(@Value("${servico-ja.upload.diretorio}") String diretorio) {
        this.diretorioBase = Path.of(diretorio).toAbsolutePath().normalize();
    }

    @Override
    public String salvar(MultipartFile arquivo, String subpasta) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new NegocioException("Envie um arquivo de imagem.");
        }
        String tipo = arquivo.getContentType() == null ? "" : arquivo.getContentType().toLowerCase(Locale.ROOT);
        if (!TIPOS_PERMITIDOS.contains(tipo)) {
            throw new NegocioException("Formato de imagem invalido. Envie um arquivo JPG, PNG ou WEBP.");
        }
        String extensao = extensaoDe(arquivo.getOriginalFilename(), tipo);

        try {
            Path diretorioDestino = diretorioBase.resolve(subpasta).normalize();
            if (!diretorioDestino.startsWith(diretorioBase)) {
                throw new NegocioException("Caminho de upload invalido.");
            }
            Files.createDirectories(diretorioDestino);
            String nomeArquivo = UUID.randomUUID() + "." + extensao;
            Path destino = diretorioDestino.resolve(nomeArquivo);
            arquivo.transferTo(destino);
            return PREFIXO_PUBLICO + subpasta + "/" + nomeArquivo;
        } catch (IOException e) {
            LOGGER.error("Falha ao salvar arquivo enviado em {}", subpasta, e);
            throw new NegocioException("Nao foi possivel salvar o arquivo enviado.");
        }
    }

    @Override
    public void remover(String caminhoRelativo) {
        if (caminhoRelativo == null || !caminhoRelativo.startsWith(PREFIXO_PUBLICO)) {
            return;
        }
        try {
            Path caminho = diretorioBase.resolve(caminhoRelativo.substring(PREFIXO_PUBLICO.length())).normalize();
            if (!caminho.startsWith(diretorioBase)) {
                LOGGER.warn("Tentativa de remover arquivo fora do diretorio de uploads: {}", caminhoRelativo);
                return;
            }
            Files.deleteIfExists(caminho);
        } catch (IOException e) {
            LOGGER.warn("Nao foi possivel remover o arquivo {}: {}", caminhoRelativo, e.getMessage());
        }
    }

    private String extensaoDe(String nomeOriginal, String contentType) {
        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            String ext = nomeOriginal.substring(nomeOriginal.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            if (EXTENSOES_PERMITIDAS.contains(ext)) {
                return "jpeg".equals(ext) ? "jpg" : ext;
            }
        }
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
