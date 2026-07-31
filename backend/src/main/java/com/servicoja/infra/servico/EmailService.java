package com.servicoja.infra.servico;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String usuarioSmtp;
    private final boolean habilitado;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String usuarioSmtp) {
        this.mailSender = mailSender;
        this.usuarioSmtp = usuarioSmtp;
        this.habilitado = usuarioSmtp != null && !usuarioSmtp.isBlank();
    }

    public void enviar(String destinatario, String assunto, String corpo) {
        if (!habilitado) {
            LOGGER.info("[EMAIL-SIMULADO] Para: {} | Assunto: {} | Corpo: {}", destinatario, assunto, corpo);
            return;
        }
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setTo(destinatario);
            mensagem.setSubject(assunto);
            mensagem.setText(corpo);
            mailSender.send(mensagem);
        } catch (Exception ex) {
            LOGGER.error("Falha ao enviar e-mail para {}: {}", destinatario, ex.getMessage());
        }
    }
}
