package com.servicoja.infra.seguranca;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LimitadorRequisicoes {

    private final Map<String, Deque<Instant>> requisicoes = new ConcurrentHashMap<>();

    public boolean permitido(String chave, int maximo, Duration janela) {
        Instant agora = Instant.now();
        Deque<Instant> historico = requisicoes.computeIfAbsent(chave, k -> new ArrayDeque<>());

        synchronized (historico) {
            while (!historico.isEmpty() && historico.peekFirst().isBefore(agora.minus(janela))) {
                historico.pollFirst();
            }
            if (historico.size() >= maximo) {
                return false;
            }
            historico.addLast(agora);
            return true;
        }
    }

    public void limpar(String chave) {
        requisicoes.remove(chave);
    }

    @Scheduled(fixedDelay = 600_000)
    public void limparChavesAntigas() {
        Instant agora = Instant.now();
        requisicoes.entrySet().removeIf(entrada -> {
            synchronized (entrada.getValue()) {
                Deque<Instant> historico = entrada.getValue();
                if (historico.isEmpty()) {
                    return true;
                }
                boolean antiga = historico.peekLast().isBefore(agora.minus(Duration.ofHours(1)));
                if (antiga) {
                    return true;
                }
                historico.removeIf(instante -> instante.isBefore(agora.minus(Duration.ofHours(1))));
                return false;
            }
        });
    }
}
