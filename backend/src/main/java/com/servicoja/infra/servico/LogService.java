package com.servicoja.infra.servico;

import com.servicoja.dominio.log.Log;
import com.servicoja.dominio.log.LogRepository;
import com.servicoja.dominio.usuario.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Transactional
    public void registrar(Usuario usuario, String acao, String detalhes, HttpServletRequest request) {
        String ip = request != null ? request.getRemoteAddr() : null;
        logRepository.save(new Log(usuario, acao, detalhes, ip));
    }

    @Transactional
    public void registrar(Usuario usuario, String acao, String detalhes) {
        logRepository.save(new Log(usuario, acao, detalhes, null));
    }
}
