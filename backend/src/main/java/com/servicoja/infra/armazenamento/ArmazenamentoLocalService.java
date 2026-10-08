package com.servicoja.infra.armazenamento;

import com.servicoja.infra.excecao.NegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ArmazenamentoLocalService implements ArmazenamentoArquivos {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArmazenamentoLocalService.class);
    private static final String PREFIXO_PUBLICO = "/uploads/";

    private final Path diretorioBase;

    public ArmazenamentoLocalService(@Value("${servico-ja.upload.diretorio}") String diretorio) {
        this.diretorioBase = Path.of(diretorio).toAbsolutePath().normalize();
    }

    @Override
    public String salvar(MultipartFile arquivo, String subpasta) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new NegocioException("Envie um arquivo de imagem.");
        }
        try {
            FormatoImagem formato = FormatoImagem.detectar(lerCabecalho(arquivo))
                    .orElseThrow(() -> new NegocioException(
                            "Formato de imagem invalido. Envie um arquivo JPG, PNG ou WEBP."));
            Path diretorioDestino = resolverDentroDaBase(subpasta);
            Files.createDirectories(diretorioDestino);
            String nomeArquivo = UUID.randomUUID() + "." + formato.extensao();
            arquivo.transferTo(diretorioDestino.resolve(nomeArquivo));
            return PREFIXO_PUBLICO + subpasta + "/" + nomeArquivo;
        } catch (IOException e) {
            LOGGER.error("Falha ao salvar arquivo enviado em {}", subpasta, e);
            throw new NegocioException("Nao foi possivel salvar o arquivo enviado.");
        }
    }

    @Override
    public void remover(String caminhoPublico, String subpasta) {
        if (caminhoPublico == null || !caminhoPublico.startsWith(PREFIXO_PUBLICO)) {
            return;
        }
        Path pastaPermitida = resolverDentroDaBase(subpasta);
        Path arquivo = diretorioBase.resolve(caminhoPublico.substring(PREFIXO_PUBLICO.length())).normalize();
        if (!arquivo.startsWith(pastaPermitida) || arquivo.equals(pastaPermitida)) {
            LOGGER.warn("Remocao ignorada: {} esta fora da pasta permitida {}", caminhoPublico, subpasta);
            return;
        }
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            LOGGER.warn("Nao foi possivel remover o arquivo {}: {}", caminhoPublico, e.getMessage());
        }
    }

    @Override
    public void removerPasta(String subpasta) {
        Path pasta = resolverDentroDaBase(subpasta);
        if (pasta.equals(diretorioBase) || !Files.isDirectory(pasta)) {
            return;
        }
        try (Stream<Path> caminhos = Files.walk(pasta)) {
            caminhos.sorted(Comparator.reverseOrder()).forEach(this::apagar);
        } catch (IOException e) {
            LOGGER.warn("Nao foi possivel remover a pasta {}: {}", subpasta, e.getMessage());
        }
    }

    private byte[] lerCabecalho(MultipartFile arquivo) throws IOException {
        try (InputStream entrada = arquivo.getInputStream()) {
            return entrada.readNBytes(FormatoImagem.TAMANHO_CABECALHO);
        }
    }

    private Path resolverDentroDaBase(String subpasta) {
        Path pasta = diretorioBase.resolve(subpasta).normalize();
        if (!pasta.startsWith(diretorioBase)) {
            throw new IllegalArgumentException("Subpasta fora do diretorio de uploads: " + subpasta);
        }
        return pasta;
    }

    private void apagar(Path caminho) {
        try {
            Files.deleteIfExists(caminho);
        } catch (IOException e) {
            LOGGER.warn("Nao foi possivel remover {}: {}", caminho, e.getMessage());
        }
    }
}
