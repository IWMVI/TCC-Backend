package br.edu.fateczl.tcc.util;

import br.edu.fateczl.tcc.domain.Aluguel;
import br.edu.fateczl.tcc.domain.Cliente;
import br.edu.fateczl.tcc.domain.ItemAluguel;
import br.edu.fateczl.tcc.domain.Traje;
import br.edu.fateczl.tcc.domain.factory.ClienteFactory;
import br.edu.fateczl.tcc.dto.aluguel.AluguelRequest;
import br.edu.fateczl.tcc.dto.aluguel.AluguelUpdateRequest;
import br.edu.fateczl.tcc.dto.aluguel.ItemAluguelRequest;
import br.edu.fateczl.tcc.enums.CondicaoTraje;
import br.edu.fateczl.tcc.enums.CorTraje;
import br.edu.fateczl.tcc.enums.EstampaTraje;
import br.edu.fateczl.tcc.enums.SexoEnum;
import br.edu.fateczl.tcc.enums.StatusAluguel;
import br.edu.fateczl.tcc.enums.StatusTraje;
import br.edu.fateczl.tcc.enums.TamanhoTraje;
import br.edu.fateczl.tcc.enums.TecidoTraje;
import br.edu.fateczl.tcc.enums.TexturaTraje;
import br.edu.fateczl.tcc.enums.TipoOcasiao;
import br.edu.fateczl.tcc.enums.TipoTraje;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder fluente para montar objetos usados nos testes de Aluguel.
 *
 * Uso típico:
 *   AluguelRequest req = AlugueisDataBuilder.umAluguel()
 *           .comClienteId(1L)
 *           .comDatas(hoje, hoje.plusDays(3))
 *           .comItem(10L)
 *           .buildRequest();
 *
 * Valores default preenchem todos os campos obrigatórios, então os testes
 * só precisam sobrescrever o que for relevante para cada cenário.
 */
public class AlugueisDataBuilder {

    public static final Long CLIENTE_ID_DEFAULT = 1L;
    public static final Long TRAJE_ID_DEFAULT = 10L;
    public static final Long ALUGUEL_ID_DEFAULT = 100L;
    public static final BigDecimal VALOR_TRAJE_DEFAULT = new BigDecimal("100.00");

    private Long id = ALUGUEL_ID_DEFAULT;
    private Long clienteId = CLIENTE_ID_DEFAULT;
    private LocalDate dataRetirada = LocalDate.now().plusDays(1);
    private LocalDate dataDevolucao = LocalDate.now().plusDays(7);
    private BigDecimal valorTotal = VALOR_TRAJE_DEFAULT;
    private BigDecimal valorDesconto = BigDecimal.ZERO;
    private String observacoes = "Observacao de teste";
    private TipoOcasiao ocasiao = TipoOcasiao.FORMATURA;
    private StatusAluguel status = StatusAluguel.ATIVO;
    private List<ItemAluguelRequest> itens = new ArrayList<>(List.of(new ItemAluguelRequest(TRAJE_ID_DEFAULT)));

    private AlugueisDataBuilder() {
    }

    public static AlugueisDataBuilder umAluguel() {
        return new AlugueisDataBuilder();
    }

    // =========================================================
    // Métodos fluentes
    // =========================================================

    public AlugueisDataBuilder comId(Long id) {
        this.id = id;
        return this;
    }

    public AlugueisDataBuilder comClienteId(Long clienteId) {
        this.clienteId = clienteId;
        return this;
    }

    public AlugueisDataBuilder comDataRetirada(LocalDate dataRetirada) {
        this.dataRetirada = dataRetirada;
        return this;
    }

    public AlugueisDataBuilder comDataDevolucao(LocalDate dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
        return this;
    }

    public AlugueisDataBuilder comDatas(LocalDate dataRetirada, LocalDate dataDevolucao) {
        this.dataRetirada = dataRetirada;
        this.dataDevolucao = dataDevolucao;
        return this;
    }

    public AlugueisDataBuilder comValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
        return this;
    }

    public AlugueisDataBuilder comDescontoNulo() {
        this.valorDesconto = null;
        return this;
    }

    public AlugueisDataBuilder comObservacoes(String observacoes) {
        this.observacoes = observacoes;
        return this;
    }

    public AlugueisDataBuilder comOcasiao(TipoOcasiao ocasiao) {
        this.ocasiao = ocasiao;
        return this;
    }

    public AlugueisDataBuilder comStatus(StatusAluguel status) {
        this.status = status;
        return this;
    }

    public AlugueisDataBuilder comItem(Long trajeId) {
        this.itens = new ArrayList<>(List.of(new ItemAluguelRequest(trajeId)));
        return this;
    }

    public AlugueisDataBuilder comItens(Long... trajesIds) {
        this.itens = Arrays.stream(trajesIds)
                .map(ItemAluguelRequest::new)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return this;
    }

    public AlugueisDataBuilder semItens() {
        this.itens = new ArrayList<>();
        return this;
    }

    public AlugueisDataBuilder comValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
        return this;
    }

    public AlugueisDataBuilder semValorTotal() {
        this.valorTotal = null;
        return this;
    }

    public AlugueisDataBuilder semDataRetirada() {
        this.dataRetirada = null;
        return this;
    }

    public AlugueisDataBuilder semDataDevolucao() {
        this.dataDevolucao = null;
        return this;
    }

    public AlugueisDataBuilder comObservacoesNulas() {
        this.observacoes = null;
        return this;
    }

    public AlugueisDataBuilder comObservacoesEmBranco() {
        this.observacoes = "   ";
        return this;
    }

    // =========================================================
    // Terminais
    // =========================================================

    public AluguelRequest buildRequest() {
        return new AluguelRequest(
                clienteId,
                dataRetirada,
                dataDevolucao,
                valorDesconto,
                observacoes,
                ocasiao,
                itens
        );
    }

    public AluguelUpdateRequest buildUpdateRequest() {
        return new AluguelUpdateRequest(
                dataRetirada,
                dataDevolucao,
                valorDesconto,
                observacoes,
                status,
                ocasiao,
                itens
        );
    }

    /**
     * Monta uma entidade Aluguel pronta para uso em mocks (findById etc).
     * Recebe o cliente para garantir consistência referencial.
     */
    public Aluguel buildEntity(Cliente cliente) {
        Aluguel aluguel = Aluguel.builder()
                .id(id)
                .cliente(cliente)
                .dataAluguel(LocalDate.now())
                .dataRetirada(dataRetirada)
                .dataDevolucao(dataDevolucao)
                .valorTotal(valorTotal)
                .valorDesconto(valorDesconto)
                .observacoes(observacoes)
                .status(status)
                .ocasiao(ocasiao)
                .build();
        return aluguel;
    }

    /**
     * Variante que inclui itens já materializados na lista.
     */
    public Aluguel buildEntityComItens(Cliente cliente, List<Traje> trajes) {
        Aluguel aluguel = buildEntity(cliente);
        for (Traje traje : trajes) {
            ItemAluguel item = ItemAluguel.builder()
                    .aluguel(aluguel)
                    .traje(traje)
                    .build();
            aluguel.getItens().add(item);
        }
        return aluguel;
    }

    /**
     * Variante que inclui {@code qtd} itens com {@code traje = null} — usado
     * para isolar a validação de "item sem traje associado".
     */
    public Aluguel buildEntityComItensSemTraje(Cliente cliente, int qtd) {
        Aluguel aluguel = buildEntity(cliente);
        for (int i = 0; i < qtd; i++) {
            ItemAluguel item = ItemAluguel.builder()
                    .aluguel(aluguel)
                    .traje(null)
                    .build();
            aluguel.getItens().add(item);
        }
        return aluguel;
    }

    // =========================================================
    // Helpers estáticos para Cliente e Traje (usados em mocks)
    // =========================================================

    public static Cliente umClienteExistente(Long id) {
        return ClienteFactory.criar()
                .comId(id)
                .comNome("Cliente Teste " + id)
                .comCpfCnpj("12345678901")
                .comEmail("cliente" + id + "@teste.com")
                .comCelular("11999999999")
                .comSexo(SexoEnum.MASCULINO)
                .construir();
    }

    public static Traje umTrajeDisponivel(Long id) {
        return umTrajeDisponivel(id, VALOR_TRAJE_DEFAULT);
    }

    public static Traje umTrajeDisponivel(Long id, BigDecimal valor) {
        return Traje.builder()
                .id(id)
                .nome("Traje " + id)
                .descricao("Traje de teste " + id)
                .tamanho(TamanhoTraje.M)
                .cor(CorTraje.PRETO)
                .tipo(TipoTraje.TERNO)
                .genero(SexoEnum.MASCULINO)
                .valorItem(valor)
                .status(StatusTraje.DISPONIVEL)
                .tecido(TecidoTraje.ALGODAO)
                .estampa(EstampaTraje.LISA)
                .textura(TexturaTraje.LISO)
                .condicao(CondicaoTraje.NOVO)
                .build();
    }

    public static Traje umTrajeIndisponivel(Long id) {
        Traje traje = umTrajeDisponivel(id);
        traje.setStatus(StatusTraje.MANUTENCAO);
        return traje;
    }

    public static Traje umTrajeAlugado(Long id) {
        Traje traje = umTrajeDisponivel(id);
        traje.setStatus(StatusTraje.ALUGADO);
        return traje;
    }
}
