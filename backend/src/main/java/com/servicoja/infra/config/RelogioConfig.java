package com.servicoja.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Relogio da aplicacao no fuso dos usuarios (padrao: America/Sao_Paulo), usado em regras que
 * dependem da hora local, como "aberto agora". Injetavel para que os testes fixem o horario.
 */
@Configuration
public class RelogioConfig {

    @Bean
    public Clock relogio(@Value("${servico-ja.app.fuso-horario}") String fusoHorario) {
        return Clock.system(ZoneId.of(fusoHorario));
    }
}
