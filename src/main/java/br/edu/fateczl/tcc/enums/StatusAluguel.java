package br.edu.fateczl.tcc.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusAluguel implements DisplayEnum {
    ATIVO("Ativo"),
    ATRASO("Em Atraso"),
    CONCLUIDO("Concluído"),
    CANCELADO("Cancelado");

    private final String nomeExibicao;

    StatusAluguel(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }

    @Override
    @JsonValue
    public String getNomeExibicao() {
        return nomeExibicao;
    }

    @JsonCreator
    public static StatusAluguel fromValue(String value) {
        return EnumUtils.fromValue(StatusAluguel.class, value);
    }
}
