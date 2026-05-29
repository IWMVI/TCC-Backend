package br.edu.fateczl.tcc.config;

import br.edu.fateczl.tcc.service.EmailService;
import br.edu.fateczl.tcc.service.LoggingEmailService;
import br.edu.fateczl.tcc.service.SmtpEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class EmailConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.mail.host")
    public EmailService smtpEmailService(@Autowired(required = false) JavaMailSender mailSender) {
        if (mailSender == null) {
            return new LoggingEmailService();
        }
        return new SmtpEmailService(mailSender);
    }

    @Bean
    @ConditionalOnMissingBean(EmailService.class)
    public EmailService loggingEmailService() {
        return new LoggingEmailService();
    }
}
