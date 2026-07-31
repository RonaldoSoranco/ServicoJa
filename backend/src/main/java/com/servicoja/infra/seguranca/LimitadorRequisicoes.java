package com.servicoja.infra.seguranca;

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
}
