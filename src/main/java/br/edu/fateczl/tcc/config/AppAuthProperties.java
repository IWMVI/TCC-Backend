package br.edu.fateczl.tcc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AppAuthProperties {

    private String jwtSecret;
    private long jwtExpirationMs = 86400000L;
    private boolean registroPublico = false;
    private String adminEmail = "admin@locadora.local";
    private String adminSenha = "Admin@123";
    private String adminNome = "Administrador";

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public void setJwtExpirationMs(long jwtExpirationMs) {
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public boolean isRegistroPublico() {
        return registroPublico;
    }

    public void setRegistroPublico(boolean registroPublico) {
        this.registroPublico = registroPublico;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAdminSenha() {
        return adminSenha;
    }

    public void setAdminSenha(String adminSenha) {
        this.adminSenha = adminSenha;
    }

    public String getAdminNome() {
        return adminNome;
    }

    public void setAdminNome(String adminNome) {
        this.adminNome = adminNome;
    }
}
