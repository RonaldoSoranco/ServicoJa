package com.servicoja.infra;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Adia efeitos colaterais que nao podem ser desfeitos (ex.: apagar arquivos do disco) para depois
 * do commit da transacao atual. Se a transacao for revertida, a acao simplesmente nao acontece.
 */
public final class AposCommit {

    private AposCommit() {
    }

    public static void executar(Runnable acao) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            acao.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                acao.run();
            }
        });
    }
}
