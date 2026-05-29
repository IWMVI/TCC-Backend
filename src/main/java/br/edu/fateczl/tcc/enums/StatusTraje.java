package br.edu.fateczl.tcc.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusTraje implements DisplayEnum {
    DISPONIVEL("Disponível"),
    ALUGADO("Alugado"),
    RESERVADO("Reservado"),
    LIMPEZA("Limpeza"),
    MANUTENCAO("Manutenção"),
    DANIFICADO("Danificado"),
    EM_CONFERENCIA("Em conferência"),
    BLOQUEADO("Bloqueado");

    private final String nomeExibicao;

    StatusTraje(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }

    @Override
    @JsonValue
    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public boolean permiteAluguel() {
        return this == DISPONIVEL;
    }

    @JsonCreator
    public static StatusTraje fromValue(String value) {
        return EnumUtils.fromValue(StatusTraje.class, value);
    }
}
