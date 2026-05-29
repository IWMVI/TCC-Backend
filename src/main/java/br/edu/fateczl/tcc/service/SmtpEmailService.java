package br.edu.fateczl.tcc.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarConfirmacaoEmail(String destinatario, String nome, String linkConfirmacao) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destinatario);
        mensagem.setSubject("Confirme seu e-mail - Locadora de Trajes");
        mensagem.setText("""
                Olá, %s!

                Para ativar sua conta no sistema da locadora, acesse o link abaixo:

                %s

                Se você não solicitou este cadastro, ignore este e-mail.
                """.formatted(nome, linkConfirmacao));
        mailSender.send(mensagem);
    }

    @Override
    public void enviarRecuperacaoSenha(String destinatario, String nome, String linkRecuperacao) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destinatario);
        mensagem.setSubject("Recuperação de senha - Locadora de Trajes");
        mensagem.setText("""
                Olá, %s!

                Para definir uma nova senha, acesse o link abaixo:

                %s

                Se você não solicitou a recuperação, ignore este e-mail.
                """.formatted(nome, linkRecuperacao));
        mailSender.send(mensagem);
    }
}
