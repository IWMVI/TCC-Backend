package br.edu.fateczl.tcc.domain;

import br.edu.fateczl.tcc.enums.CondicaoTraje;
import br.edu.fateczl.tcc.enums.CorTraje;
import br.edu.fateczl.tcc.enums.EstampaTraje;
import br.edu.fateczl.tcc.enums.SexoEnum;
import br.edu.fateczl.tcc.enums.StatusTraje;
import br.edu.fateczl.tcc.enums.TamanhoTraje;
import br.edu.fateczl.tcc.enums.TecidoTraje;
import br.edu.fateczl.tcc.enums.TexturaTraje;
import br.edu.fateczl.tcc.enums.TipoTraje;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "traje")
@Table(name = "traje")
public class Traje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String descricao;

    @Column(length = 2, nullable = false)
    @Enumerated(EnumType.STRING)
    private TamanhoTraje tamanho;

    @Column(length = 8, nullable = false)
    @Enumerated(EnumType.STRING)
    private CorTraje cor;

    @Column(length = 8, nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoTraje tipo;

    @Column(length = 9, nullable = false)
    @Enumerated(EnumType.STRING)
    private SexoEnum genero;

    @Column(precision = 8, scale = 2, nullable = false)
    private BigDecimal valorItem;

    @Column(length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusTraje status;

    @Column(length = 50, nullable = false)
    private String nome;

    @Column(length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private TecidoTraje tecido;

    @Column(length = 17, nullable = false)
    @Enumerated(EnumType.STRING)
    private EstampaTraje estampa;

    @Column(length = 9, nullable = false)
    @Enumerated(EnumType.STRING)
    private TexturaTraje textura;

    @Column(length = 13, nullable = false)
    @Enumerated(EnumType.STRING)
    private CondicaoTraje condicao;

    @Column(columnDefinition = "LONGTEXT")
    private String imagemUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
    }

    public Traje() {
    }

    public Traje(Long id, String descricao, TamanhoTraje tamanho, CorTraje cor, TipoTraje tipo, SexoEnum genero,
            BigDecimal valorItem, StatusTraje status, String nome, TecidoTraje tecido, EstampaTraje estampa,
            TexturaTraje textura, CondicaoTraje condicao, String imagemUrl) {
        this.id = id;
        this.descricao = descricao;
        this.tamanho = tamanho;
        this.cor = cor;
        this.tipo = tipo;
        this.genero = genero;
        this.valorItem = valorItem;
        this.status = status;
        this.nome = nome;
        this.tecido = tecido;
        this.estampa = estampa;
        this.textura = textura;
        this.condicao = condicao;
        this.imagemUrl = imagemUrl;
    }

    public static TrajeBuilder builder() {
        return new TrajeBuilder();
    }

    public static class TrajeBuilder {
        private Long id;
        private String descricao;
        private TamanhoTraje tamanho;
        private CorTraje cor;
        private TipoTraje tipo;
        private SexoEnum genero;
        private BigDecimal valorItem;
        private StatusTraje status;
        private String nome;
        private TecidoTraje tecido;
        private EstampaTraje estampa;
        private TexturaTraje textura;
        private CondicaoTraje condicao;
        private String imagemUrl;

        public TrajeBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TrajeBuilder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public TrajeBuilder tamanho(TamanhoTraje tamanho) {
            this.tamanho = tamanho;
            return this;
        }

        public TrajeBuilder cor(CorTraje cor) {
            this.cor = cor;
            return this;
        }

        public TrajeBuilder tipo(TipoTraje tipo) {
            this.tipo = tipo;
            return this;
        }

        public TrajeBuilder genero(SexoEnum genero) {
            this.genero = genero;
            return this;
        }

        public TrajeBuilder valorItem(BigDecimal valorItem) {
            this.valorItem = valorItem;
            return this;
        }

        public TrajeBuilder status(StatusTraje status) {
            this.status = status;
            return this;
        }

        public TrajeBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public TrajeBuilder tecido(TecidoTraje tecido) {
            this.tecido = tecido;
            return this;
        }

        public TrajeBuilder estampa(EstampaTraje estampa) {
            this.estampa = estampa;
            return this;
        }

        public TrajeBuilder textura(TexturaTraje textura) {
            this.textura = textura;
            return this;
        }

        public TrajeBuilder condicao(CondicaoTraje condicao) {
            this.condicao = condicao;
            return this;
        }

        public TrajeBuilder imagemUrl(String imagemUrl) {
            this.imagemUrl = imagemUrl;
            return this;
        }

        public Traje build() {
            Traje traje = new Traje();
            traje.setId(this.id);
            traje.setDescricao(this.descricao);
            traje.setTamanho(this.tamanho);
            traje.setCor(this.cor);
            traje.setTipo(this.tipo);
            traje.setGenero(this.genero);
            traje.setValorItem(this.valorItem);
            traje.setStatus(this.status);
            traje.setNome(this.nome);
            traje.setTecido(this.tecido);
            traje.setEstampa(this.estampa);
            traje.setTextura(this.textura);
            traje.setCondicao(this.condicao);
            traje.setImagemUrl(this.imagemUrl);
            return traje;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public TamanhoTraje getTamanho() {
        return tamanho;
    }

    public void setTamanho(TamanhoTraje tamanho) {
        this.tamanho = tamanho;
    }

    public CorTraje getCor() {
        return cor;
    }

    public void setCor(CorTraje cor) {
        this.cor = cor;
    }

    public TipoTraje getTipo() {
        return tipo;
    }

    public void setTipo(TipoTraje tipo) {
        this.tipo = tipo;
    }

    public SexoEnum getGenero() {
        return genero;
    }

    public void setGenero(SexoEnum genero) {
        this.genero = genero;
    }

    public BigDecimal getValorItem() {
        return valorItem;
    }

    public void setValorItem(BigDecimal valorItem) {
        this.valorItem = valorItem;
    }

    public StatusTraje getStatus() {
        return status;
    }

    public void setStatus(StatusTraje status) {
        this.status = status;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TecidoTraje getTecido() {
        return tecido;
    }

    public void setTecido(TecidoTraje tecido) {
        this.tecido = tecido;
    }

    public EstampaTraje getEstampa() {
        return estampa;
    }

    public void setEstampa(EstampaTraje estampa) {
        this.estampa = estampa;
    }

    public TexturaTraje getTextura() {
        return textura;
    }

    public void setTextura(TexturaTraje textura) {
        this.textura = textura;
    }

    public CondicaoTraje getCondicao() {
        return condicao;
    }

    public void setCondicao(CondicaoTraje condicao) {
        this.condicao = condicao;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public void atualizar(String descricao, TamanhoTraje tamanho, CorTraje cor, TipoTraje tipo, SexoEnum genero,
            BigDecimal valorItem, StatusTraje status, String nome, TecidoTraje tecido, EstampaTraje estampa,
            TexturaTraje textura, CondicaoTraje condicao, String imagemUrl) {
        this.descricao = descricao;
        this.tamanho = tamanho;
        this.cor = cor;
        this.tipo = tipo;
        this.imagemUrl = imagemUrl;
        this.genero = genero;
        this.valorItem = valorItem;
        this.status = status;
        this.nome = nome;
        this.tecido = tecido;
        this.estampa = estampa;
        this.textura = textura;
        this.condicao = condicao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Traje traje = (Traje) o;
        return Objects.equals(id, traje.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Traje{" +
                "id=" + id +
                ", descricao='" + descricao + '\'' +
                ", tamanho=" + tamanho +
                ", cor=" + cor +
                ", tipo=" + tipo +
                ", genero=" + genero +
                ", imagemUrl=" + (imagemUrl != null ? "present" : "null") +
                ", valorItem=" + valorItem +
                ", status=" + status +
                ", nome='" + nome + '\'' +
                ", tecido=" + tecido +
                ", estampa=" + estampa +
                ", textura=" + textura +
                ", condicao=" + condicao +
                '}';
    }
}