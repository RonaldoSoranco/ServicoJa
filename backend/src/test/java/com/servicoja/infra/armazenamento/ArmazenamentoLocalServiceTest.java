package com.servicoja.infra.armazenamento;

import com.servicoja.infra.excecao.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArmazenamentoLocalServiceTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13, 'I', 'H', 'D', 'R'};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F', 0, 1};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 36, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' '};

    @TempDir
    Path diretorio;

    private ArmazenamentoLocalService armazenamento;

    @BeforeEach
    void preparar() {
        armazenamento = new ArmazenamentoLocalService(diretorio.toString());
    }

    @Test
    void salvaImagensValidasComExtensaoDoFormatoReal() {
        assertThat(armazenamento.salvar(arquivo("foto.png", "image/png", PNG), "empresas/1/fotos")).endsWith(".png");
        assertThat(armazenamento.salvar(arquivo("foto.jpeg", "image/jpeg", JPEG), "empresas/1/fotos")).endsWith(".jpg");
        assertThat(armazenamento.salvar(arquivo("foto.webp", "image/webp", WEBP), "empresas/1/fotos")).endsWith(".webp");
    }

    @Test
    void ignoraNomeETipoInformadosPeloCliente() {
        String caminho = armazenamento.salvar(arquivo("foto.png", "image/png", JPEG), "empresas/1/fotos");

        assertThat(caminho).startsWith("/uploads/empresas/1/fotos/").endsWith(".jpg");
        assertThat(Files.exists(diretorio.resolve(caminho.substring("/uploads/".length())))).isTrue();
    }

    @Test
    void recusaArquivoQueNaoEImagemMesmoDeclaradoComoImagem() {
        byte[] html = "<html><script>alert(1)</script></html>".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> armazenamento.salvar(arquivo("foto.png", "image/png", html), "empresas/1/fotos"))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Formato de imagem invalido");
    }

    @Test
    void recusaArquivoVazio() {
        assertThatThrownBy(() -> armazenamento.salvar(arquivo("foto.png", "image/png", new byte[0]), "empresas/1/fotos"))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void removeArquivoDaPropriaPasta() {
        String caminho = armazenamento.salvar(arquivo("logo.png", "image/png", PNG), "empresas/1/logo");

        armazenamento.remover(caminho, "empresas/1");

        assertThat(Files.exists(diretorio.resolve(caminho.substring("/uploads/".length())))).isFalse();
    }

    @Test
    void naoRemoveArquivoDeOutraEmpresa() {
        String caminhoDeOutraEmpresa = armazenamento.salvar(arquivo("foto.png", "image/png", PNG), "empresas/2/fotos");
        Path arquivo = diretorio.resolve(caminhoDeOutraEmpresa.substring("/uploads/".length()));

        armazenamento.remover(caminhoDeOutraEmpresa, "empresas/1");
        armazenamento.remover("/uploads/empresas/1/logo/../../2/fotos/" + arquivo.getFileName(), "empresas/1");
        armazenamento.remover("/uploads/empresas/10/../2/fotos/" + arquivo.getFileName(), "empresas/1");

        assertThat(Files.exists(arquivo)).isTrue();
    }

    @Test
    void removerPastaApagaTodosOsArquivosDaEmpresa() {
        armazenamento.salvar(arquivo("logo.png", "image/png", PNG), "empresas/1/logo");
        armazenamento.salvar(arquivo("foto.jpg", "image/jpeg", JPEG), "empresas/1/fotos");
        String deOutraEmpresa = armazenamento.salvar(arquivo("foto.jpg", "image/jpeg", JPEG), "empresas/2/fotos");

        armazenamento.removerPasta("empresas/1");

        assertThat(Files.exists(diretorio.resolve("empresas/1"))).isFalse();
        assertThat(Files.exists(diretorio.resolve(deOutraEmpresa.substring("/uploads/".length())))).isTrue();
    }

    @Test
    void recusaSubpastaForaDoDiretorioDeUploads() {
        assertThatThrownBy(() -> armazenamento.removerPasta("../fora"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private MockMultipartFile arquivo(String nome, String tipo, byte[] conteudo) {
        return new MockMultipartFile("arquivo", nome, tipo, conteudo);
    }
}
