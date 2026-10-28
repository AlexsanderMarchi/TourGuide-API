package br.com.tourguide.api.TourGuideApiApplication.infra.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Envia emails pela configuração spring.mail.* do application.properties */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${api.mail.remetente}")
    private String remetente;

    @Value("${api.frontend.url}")
    private String urlFrontend;

    public void enviarEmailRecuperacaoSenha(String destinatario, String token, long minutosValidade) {
        var link = urlFrontend + "/recuperar-senha?token=" + token;

        var mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("Tour.Guide - Recuperação de senha");
        mensagem.setText("""
                Olá!

                Recebemos um pedido para redefinir a sua senha. Acesse o link abaixo para criar uma nova:

                %s

                O link expira em %d minutos. Se não foi você que pediu, ignore este email.
                """.formatted(link, minutosValidade));

        mailSender.send(mensagem);
    }
}
