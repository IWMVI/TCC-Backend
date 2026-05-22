package br.edu.fateczl.tcc.domain;

import br.edu.fateczl.tcc.enums.StatusAluguel;
import br.edu.fateczl.tcc.enums.TipoOcasiao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "aluguel")
@Table(name = "aluguel")
public class Aluguel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dataAluguel;

    @Column(nullable = false)
    private LocalDate dataRetirada;

    @Column(nullable = false)
    private LocalDate dataDevolucao;

    @Column(precision = 8, scale = 2, nullable = false)
    private BigDecimal valorTotal;

    @Column(precision = 8, scale = 2)
    private BigDecimal valorDesconto;

    @Column(precision = 8, scale = 2, nullable = false)
    private BigDecimal valorMulta = BigDecimal.ZERO;

    @Column(length = 200)
    private String observacoes;

    @Column(length = 9, nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusAluguel status;

    @Column(length = 18)
    @Enumerated(EnumType.STRING)
    private TipoOcasiao ocasiao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "aluguel", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemAluguel> itens = new ArrayList<>();


    public Aluguel() {
    }

    public Aluguel(Long id, LocalDate dataAluguel, LocalDate dataRetirada, LocalDate dataDevolucao, BigDecimal valorTotal, BigDecimal valorDesconto, String observacoes, StatusAluguel status, TipoOcasiao ocasiao, Cliente cliente) {
        this.id = id;
        this.dataAluguel = dataAluguel;
        this.dataRetirada = dataRetirada;
        this.dataDevolucao = dataDevolucao;
        this.valorTotal = valorTotal;
        this.valorDesconto = valorDesconto;
        this.observacoes = observacoes;
        this.status = status;
        this.ocasiao = ocasiao;
        this.cliente = cliente;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataAluguel() {
        return dataAluguel;
    }

    public void setDataAluguel(LocalDate dataAluguel) {
        this.dataAluguel = dataAluguel;
    }

    public LocalDate getDataRetirada() {
        return dataRetirada;
    }

    public void setDataRetirada(LocalDate dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(LocalDate dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorDesconto() {
        return valorDesconto;
    }

    public void setValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public BigDecimal getValorMulta() {
        return valorMulta;
    }

    public void setValorMulta(BigDecimal valorMulta) {
        this.valorMulta = valorMulta != null ? valorMulta : BigDecimal.ZERO;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public StatusAluguel getStatus() {
        return status;
    }

    public void setStatus(StatusAluguel status) {
        this.status = status;
    }

    public TipoOcasiao getOcasiao() {
        return ocasiao;
    }

    public void setOcasiao(TipoOcasiao ocasiao) {
        this.ocasiao = ocasiao;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<ItemAluguel> getItens() {
        return itens;
    }

    public void setItens(List<ItemAluguel> itens) {
        this.itens = itens;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aluguel alquiler = (Aluguel) o;
        return Objects.equals(id, alquiler.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Aluguel{" +
                "id=" + id +
                ", dataAluguel=" + dataAluguel +
                ", dataRetirada=" + dataRetirada +
                ", dataDevolucao=" + dataDevolucao +
                ", valorTotal=" + valorTotal +
                ", valorDesconto=" + valorDesconto +
                ", observacoes='" + observacoes + '\'' +
                ", status=" + status +
                ", ocasiao=" + ocasiao +
                ", cliente=" + cliente +
                '}';
    }

    public static AluguelBuilder builder() {
        return new AluguelBuilder();
    }

    public static class AluguelBuilder {
        private Long id;
        private LocalDate dataAluguel;
        private LocalDate dataRetirada;
        private LocalDate dataDevolucao;
        private BigDecimal valorTotal;
        private BigDecimal valorDesconto;
        private BigDecimal valorMulta = BigDecimal.ZERO;
        private String observacoes;
        private StatusAluguel status;
        private TipoOcasiao ocasiao;
        private Cliente cliente;

        public AluguelBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AluguelBuilder dataAluguel(LocalDate dataAluguel) {
            this.dataAluguel = dataAluguel;
            return this;
        }

        public AluguelBuilder dataRetirada(LocalDate dataRetirada) {
            this.dataRetirada = dataRetirada;
            return this;
        }

        public AluguelBuilder dataDevolucao(LocalDate dataDevolucao) {
            this.dataDevolucao = dataDevolucao;
            return this;
        }

        public AluguelBuilder valorTotal(BigDecimal valorTotal) {
            this.valorTotal = valorTotal;
            return this;
        }

        public AluguelBuilder valorDesconto(BigDecimal valorDesconto) {
            this.valorDesconto = valorDesconto;
            return this;
        }

        public AluguelBuilder valorMulta(BigDecimal valorMulta) {
            this.valorMulta = valorMulta != null ? valorMulta : BigDecimal.ZERO;
            return this;
        }

        public AluguelBuilder observacoes(String observacoes) {
            this.observacoes = observacoes;
            return this;
        }

        public AluguelBuilder status(StatusAluguel status) {
            this.status = status;
            return this;
        }

        public AluguelBuilder ocasiao(TipoOcasiao ocasiao) {
            this.ocasiao = ocasiao;
            return this;
        }

        public AluguelBuilder cliente(Cliente cliente) {
            this.cliente = cliente;
            return this;
        }

        public Aluguel build() {
            Aluguel aluguel = new Aluguel();
            aluguel.id = this.id;
            aluguel.dataAluguel = this.dataAluguel;
            aluguel.dataRetirada = this.dataRetirada;
            aluguel.dataDevolucao = this.dataDevolucao;
            aluguel.valorTotal = this.valorTotal;
            aluguel.valorDesconto = this.valorDesconto;
            aluguel.valorMulta = this.valorMulta;
            aluguel.observacoes = this.observacoes;
            aluguel.status = this.status;
            aluguel.ocasiao = this.ocasiao;
            aluguel.cliente = this.cliente;
            return aluguel;
        }
    }
}