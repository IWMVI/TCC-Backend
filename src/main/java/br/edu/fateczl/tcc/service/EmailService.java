package br.edu.fateczl.tcc.service;

public interface EmailService {

    void enviarConfirmacaoEmail(String destinatario, String nome, String linkConfirmacao);

    void enviarRecuperacaoSenha(String destinatario, String nome, String linkRecuperacao);
}
