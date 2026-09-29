package com.servicoja.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Serve os arquivos gravados por {@link com.servicoja.infra.armazenamento.ArmazenamentoLocalService}
 * publicamente em "/uploads/**".
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String diretorioUpload;

    public WebMvcConfig(@Value("${servico-ja.upload.diretorio}") String diretorioUpload) {
        this.diretorioUpload = diretorioUpload;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String localizacao = Path.of(diretorioUpload).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(localizacao);
    }
}
