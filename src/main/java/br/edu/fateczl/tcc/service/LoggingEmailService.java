package br.edu.fateczl.tcc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void enviarConfirmacaoEmail(String destinatario, String nome, String linkConfirmacao) {
        log.info("""
                ========== E-MAIL DE CONFIRMAÇÃO (modo desenvolvimento) ==========
                Para: {}
                Olá, {}!
                Confirme seu e-mail acessando o link:
                {}
                ==================================================================
                """, destinatario, nome, linkConfirmacao);
    }

    @Override
    public void enviarRecuperacaoSenha(String destinatario, String nome, String linkRecuperacao) {
        log.info("""
                ========== E-MAIL DE RECUPERAÇÃO DE SENHA (modo desenvolvimento) ==========
                Para: {}
                Olá, {}!
                Redefina sua senha acessando o link:
                {}
                =============================================================================
                """, destinatario, nome, linkRecuperacao);
    }
}
