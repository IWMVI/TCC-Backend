package br.edu.fateczl.tcc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "funcionario")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nome;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private boolean emailVerificado = false;

    @JsonIgnore
    @Column(length = 64)
    private String tokenConfirmacao;

    @JsonIgnore
    private LocalDateTime tokenExpiraEm;

    @JsonIgnore
    @Column(length = 64)
    private String tokenRecuperacaoSenha;

    @JsonIgnore
    private LocalDateTime tokenRecuperacaoExpiraEm;

    @Column(nullable = false)
    private boolean ativo = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isEmailVerificado() {
        return emailVerificado;
    }

    public void setEmailVerificado(boolean emailVerificado) {
        this.emailVerificado = emailVerificado;
    }

    public String getTokenConfirmacao() {
        return tokenConfirmacao;
    }

    public void setTokenConfirmacao(String tokenConfirmacao) {
        this.tokenConfirmacao = tokenConfirmacao;
    }

    public LocalDateTime getTokenExpiraEm() {
        return tokenExpiraEm;
    }

    public void setTokenExpiraEm(LocalDateTime tokenExpiraEm) {
        this.tokenExpiraEm = tokenExpiraEm;
    }

    public String getTokenRecuperacaoSenha() {
        return tokenRecuperacaoSenha;
    }

    public void setTokenRecuperacaoSenha(String tokenRecuperacaoSenha) {
        this.tokenRecuperacaoSenha = tokenRecuperacaoSenha;
    }

    public LocalDateTime getTokenRecuperacaoExpiraEm() {
        return tokenRecuperacaoExpiraEm;
    }

    public void setTokenRecuperacaoExpiraEm(LocalDateTime tokenRecuperacaoExpiraEm) {
        this.tokenRecuperacaoExpiraEm = tokenRecuperacaoExpiraEm;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Funcionario that = (Funcionario) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
