package br.edu.fateczl.tcc.service;

import br.edu.fateczl.tcc.domain.Aluguel;
import br.edu.fateczl.tcc.domain.Cliente;
import br.edu.fateczl.tcc.domain.ItemAluguel;
import br.edu.fateczl.tcc.domain.Traje;
import br.edu.fateczl.tcc.dto.aluguel.AluguelFiltroRequest;
import br.edu.fateczl.tcc.dto.aluguel.AluguelRequest;
import br.edu.fateczl.tcc.dto.aluguel.AluguelResponse;
import br.edu.fateczl.tcc.dto.aluguel.AluguelUpdateRequest;
import br.edu.fateczl.tcc.dto.devolucao.DevolucaoRequest;
import br.edu.fateczl.tcc.dto.devolucao.DevolucaoResponse;
import br.edu.fateczl.tcc.dto.devolucao.ItemDevolucaoRequest;
import br.edu.fateczl.tcc.enums.CondicaoTraje;
import br.edu.fateczl.tcc.enums.StatusAluguel;
import br.edu.fateczl.tcc.enums.StatusTraje;
import br.edu.fateczl.tcc.enums.TipoOcasiao;
import br.edu.fateczl.tcc.exception.BusinessException;
import br.edu.fateczl.tcc.exception.ResourceNotFoundException;
import br.edu.fateczl.tcc.repository.AluguelRepository;
import br.edu.fateczl.tcc.repository.ClienteRepository;
import br.edu.fateczl.tcc.repository.ItemAluguelRepository;
import br.edu.fateczl.tcc.repository.TrajeRepository;
import br.edu.fateczl.tcc.util.AlugueisDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static br.edu.fateczl.tcc.util.AlugueisDataBuilder.ALUGUEL_ID_DEFAULT;
import static br.edu.fateczl.tcc.util.AlugueisDataBuilder.CLIENTE_ID_DEFAULT;
import static br.edu.fateczl.tcc.util.AlugueisDataBuilder.TRAJE_ID_DEFAULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TFS — Teste Funcional Sistemático.
 *
 * Combina PCE (Particionamento em Classes de Equivalência) com AVL (Análise
 * do Valor Limite) de forma sistemática, seguindo o método proposto por
 * Delamaro/Maldonado/Jino ("Introdução ao Teste de Software"):
 *
 *   1) Identificar condições de entrada e seus domínios.
 *   2) Derivar classes de equivalência — válidas (V) e inválidas (I) — para
 *      cada condição.
 *   3) Identificar os valores limite de cada classe.
 *   4) Construir a matriz de casos de teste combinando:
 *        - Um caso "típico" com todas as classes válidas.
 *        - Casos nos limites das classes válidas (bordas inferiores/superiores).
 *        - Um caso para cada classe inválida, mantendo as demais entradas
 *          válidas — para isolar o efeito do defeito.
 *
 * =========================================================================
 * MATRIZ DE CLASSES DE EQUIVALÊNCIA (método criar)
 * =========================================================================
 *   Variável                  | Classes Válidas (V)         | Classes Inválidas (I)
 *   --------------------------|-----------------------------|------------------------------
 *   C1: clienteId             | V1 existe                   | I1 não existe
 *   C2: dataRetirada          | V2 ≥ hoje                   | I2 < hoje
 *   C3: dataDevolucao         | V3 ≥ dataRetirada           | I3 < dataRetirada
 *   C4: itens.trajeId         | V4 existe                   | I4 não existe
 *   C5: traje.status          | V5 DISPONIVEL               | I5 ≠ DISPONIVEL
 *   C6: traje no período      | V6 livre                    | I6 ocupado
 *   C7: valorDesconto         | V7 [0, total] (ou null)     | I7 > total
 *   C8: quantidade de itens   | V8 ≥ 1 (1 ou múltiplos)     | (= 0 validado no DTO)
 *
 * VALORES LIMITE RELEVANTES:
 *   - dataRetirada: hoje (borda V), ontem (borda I)
 *   - dataDevolucao: = dataRetirada (borda V inferior), dataRetirada-1 (borda I)
 *   - valorDesconto: 0 (borda V inferior), total (borda V superior),
 *                    null (equivalente a zero), total+0,01 (borda I)
 *
 * CASOS DE TESTE DERIVADOS (criar):
 *   CT1  — todas V, valores típicos                           → sucesso
 *   CT2  — todas V, bordas inferiores (retirada=hoje, dev=retirada, desconto=0) → sucesso
 *   CT3  — todas V, borda superior do desconto (=total)       → sucesso, total=0
 *   CT4  — todas V, desconto=null                             → sucesso, desconto tratado como 0
 *   CT5  — V8 múltiplos itens: soma de valorItem coerente     → valorTotal = soma dos itens
 *   CT6  — I1 isolada (cliente inexistente)                   → ResourceNotFoundException
 *   CT7  — I2 isolada na borda (retirada=ontem)               → BusinessException "passado"
 *   CT8  — I3 isolada na borda (dev=retirada-1)               → BusinessException "após a retirada"
 *   CT9  — I4 isolada (traje inexistente)                     → ResourceNotFoundException
 *   CT10 — I5 isolada (traje ALUGADO)                         → BusinessException "não está disponível"
 *   CT11 — I6 isolada (período ocupado)                       → BusinessException "alugado nesse período"
 *   CT12 — I7 isolada na borda (desconto=total+0,01)          → BusinessException "negativo"
 *
 * =========================================================================
 * MATRIZ (método atualizar) — acrescenta duas variáveis:
 *   C9: existência do aluguel: V9 existe / I9 não existe
 *   C10: status do aluguel:    V10 ATIVO  / I10 ≠ ATIVO
 *
 * CASOS DE TESTE (atualizar):
 *   CT13 — todas V                                            → sucesso
 *   CT14 — I9 (aluguel inexistente)                           → ResourceNotFoundException
 *   CT15 — I10 (aluguel CONCLUÍDO)                            → BusinessException "só ATIVOS"
 *   CT16 — I3 na borda                                        → BusinessException
 *   CT17 — I6 isolada                                         → BusinessException
 *   CT18 — I7 na borda                                        → BusinessException
 *   CT25 — V7 borda no update: desconto=null                  → sucesso, total = soma dos itens
 *
 * =========================================================================
 * Operações de leitura/remoção (buscarPorId, listarTodos, deletar) possuem
 * uma única condição de entrada (existência do ID) e são tratadas com um
 * caso V e um caso I — CT19..CT24.
 *
 * =========================================================================
 * MATRIZ (listarComFiltros) — variáveis: combinação de filtros opcionais
 *   CT26 — V típico: todos os filtros preenchidos    → Specification composta, lista mapeada
 *   CT27 — V parcial: somente status                  → Specification composta, lista mapeada
 *   CT28 — V borda: filtro totalmente nulo            → Specification inerte, lista vazia
 *
 * MATRIZ (buscarAtivoByTrajeId) — variável: existência de item ATIVO
 *   CT29 — V: existe item ATIVO                       → AluguelResponse do aluguel pai
 *   CT30 — I: não existe                              → ResourceNotFoundException
 *
 * MATRIZ (registrarDevolucao) — variáveis: existência do aluguel, status, itens
 *   CT31 — V típico: aluguel ATIVO + itens preenchidos → trajes atualizados (condicao+DISPONIVEL),
 *                                                       aluguel CONCLUIDO, devolução criada
 *   CT32 — V borda: aluguel ATIVO + itens=null         → forEach NÃO executa, aluguel CONCLUIDO
 *   CT33 — I9: aluguel inexistente                     → ResourceNotFoundException
 *   CT34 — I10: aluguel CONCLUÍDO (status≠ATIVO)       → BusinessException "aluguéis ATIVOS"
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TFS - AluguelService (Teste Funcional Sistemático)")
class AluguelServiceTest {

    @Mock
    private AluguelRepository aluguelRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private TrajeRepository trajeRepository;

    @Mock
    private ItemAluguelRepository itemAluguelRepository;

    @Mock
    private DevolucaoService devolucaoService;

    @InjectMocks
    private AluguelService service;

    private Cliente cliente;
    private Traje traje;

    @BeforeEach
    void setUp() {
        cliente = AlugueisDataBuilder.umClienteExistente(CLIENTE_ID_DEFAULT);
        traje = AlugueisDataBuilder.umTrajeDisponivel(TRAJE_ID_DEFAULT);
    }

    private void stubarCaminhoFelizCriar() {
        when(clienteRepository.findById(CLIENTE_ID_DEFAULT)).thenReturn(Optional.of(cliente));
        when(trajeRepository.findById(TRAJE_ID_DEFAULT)).thenReturn(Optional.of(traje));
        when(itemAluguelRepository.trajeIndisponivelNoPeriodo(
                eq(TRAJE_ID_DEFAULT), any(LocalDate.class), any(LocalDate.class), eq(null)))
                .thenReturn(false);
    }

    // =========================================================
    // CRIAR — CT1..CT12
    // =========================================================
    @Nested
    @DisplayName("Criar Aluguel — matriz TFS")
    class Criar {

        @Test
        @DisplayName("CT1 — todas as classes VÁLIDAS, valores típicos")
        void ct1_deve_criar_quando_todasClassesValidasEmValoresTipicos() {
            AluguelRequest request = AlugueisDataBuilder.umAluguel().buildRequest();
            stubarCaminhoFelizCriar();

            AluguelResponse response = service.criar(request);

            assertNotNull(response);
            assertEquals(CLIENTE_ID_DEFAULT, response.clienteId());
            assertEquals(StatusAluguel.ATIVO, response.status());
            assertEquals(new BigDecimal("100.00"), response.valorTotal());
            verify(aluguelRepository).save(any(Aluguel.class));
        }

        @Test
        @DisplayName("CT2 — todas VÁLIDAS nas bordas inferiores (retirada=hoje, devolução=retirada, desconto=0)")
        void ct2_deve_criar_quando_todasClassesValidasNasBordasInferiores() {
            LocalDate hoje = LocalDate.now();
            AluguelRequest request = AlugueisDataBuilder.umAluguel()
                    .comDatas(hoje, hoje)
                    .comValorDesconto(BigDecimal.ZERO)
                    .buildRequest();
            stubarCaminhoFelizCriar();

            AluguelResponse response = service.criar(request);

            assertNotNull(response);
            assertEquals(new BigDecimal("100.00"), response.valorTotal());
        }

        @Test
        @DisplayName("CT3 — todas VÁLIDAS, desconto na borda superior (= soma dos itens)")
        void ct3_deve_criar_quando_descontoIgualAoTotal() {
            AluguelRequest request = AlugueisDataBuilder.umAluguel()
                    .comValorDesconto(new BigDecimal("100.00"))
                    .buildRequest();
            stubarCaminhoFelizCriar();

            AluguelResponse response = service.criar(request);

            assertNotNull(response);
            assertEquals(0, response.valorTotal().compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("CT4 — todas VÁLIDAS, desconto=null (tratado como zero)")
        void ct4_deve_criar_quando_descontoNulo() {
            AluguelRequest request = AlugueisDataBuilder.umAluguel()
                    .comDescontoNulo()
                    .buildRequest();
            stubarCaminhoFelizCriar();

            AluguelResponse response = service.criar(request);

            assertNotNull(response);
            assertEquals(new BigDecimal("100.00"), response.valorTotal());
        }

        @Test
        @DisplayName("CT5 — V8 múltiplos itens: valorTotal soma corretamente os valorItem")
        void ct5_deve_somarValoresCorretamente_quando_multiplosItens() {
            Long trajeId2 = 11L;
            Traje traje2 = AlugueisDataBuilder.umTrajeDisponivel(trajeId2, new BigDecimal("250.50"));
            AluguelRequest request = AlugueisDataBuilder.umAluguel()
                    .comItens(TRAJE_ID_DEFAULT, trajeId2)
                    .buildRequest();

            when(clienteRepository.findById(CLIENTE_ID_DEFAULT)).thenReturn(Optional.of(cliente));
            when(trajeRepository.findById(TRAJE_ID_DEFAULT)).thenReturn(Optional.of(traje));
            when(trajeRepository.findById(trajeId2)).thenReturn(Optional.of(traje2));
            when(itemAluguelRepository.trajeIndisponivelNoPeriodo(
                    any(Long.class), any(LocalDate.class), any(LocalDate.class), eq(null)))
                    .thenReturn(false);

            AluguelResponse response = service.criar(request);

            // 100.00 + 250.50 = 350.50
            assertEquals(0, response.valorTotal().compareTo(new BigDecimal("350.50")));
        }

        @Test
        @DisplayName("CT6 — I1 isolada: cliente inexistente, demais VÁLIDAS")
        void ct6_deve_lancarResourceNotFound_quando_apenasClienteInexistente() {
            AluguelRequest request = AlugueisDataBuilder.umAluguel().buildRequest();
            when(clienteRepository.findById(CLIENTE_ID_DEFAULT)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.criar(request));
            verify(aluguelRepository, never()).save(any(Aluguel.class));
        }

        @Test
        @DisplayName("CT7 — I2 isolada na borda: dataRetirada=ontem, demais VÁLIDAS")
        void ct7_deve_lancarBusinessException_quando_apenasRetiradaNoPassado() {
            LocalDate ontem = LocalDate.now().minusDays(1);
            AluguelRequest request = AlugueisDataBuilder.umAluguel()
                    .comDatas(ontem, LocalDate.now().plusDays(3))
                    .buildRequest();
            when(clienteRepository.findById(CLIENTE_ID_DEFAULT)).thenReturn(Optional.of(cliente));

            BusinessException ex = assertThrows(BusinessException.class, () -> service.criar(request));
            assertEquals("A data de retirada não pode ser no passado", ex.getMessage());
            verify(aluguelRepository, never()).save(any(Aluguel.class));
        }

        @Test
        @DisplayName("CT8 — I3 isolada na borda: dataDevolucao=dataRetirada-1, demais VÁLIDAS")
        void ct8_deve_lancarBusinessException_quando_apenasDevolucaoAntesDaRetirada() {
            LocalDate retirada = LocalDate.now().plusDays(5);
            AluguelRequest request = AlugueisDataBuilder.umAluguel()
                    .comDatas(retirada, retirada.minusDays(1))
                    .buildRequest();
            when(clienteRepository.findById(CLIENTE_ID_DEFAULT)).thenReturn(Optional.of(cliente));

            BusinessException ex = assertThrows(BusinessException.class, () -> service.criar(request));
            assertEquals("A data de devolução deve ser após a data de retirada", ex.getMessage());
            verify(aluguelRepository, never()).save(any(Aluguel.class));
        }

        @Test
        @DisplayName("CT9 — I4 isolada: traje inexistente, demais VÁLIDAS")
        void ct9_deve_lancarResourceNotFound_quando_apenasTrajeInexistente() {
            AluguelRequest request = AlugueisDataBuilder.umAluguel().buildRequest();
            when(clienteRepository.findById(CLIENTE_ID_DEFAULT)).thenReturn(Optional.of(cliente));
            when(trajeRepository.findById(TRAJE_ID_DEFAULT)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.criar(request));
            verify(aluguelRepository, never()).save(any(Aluguel.class));
        }

        @Test
        @DisplayName("CT10 — I5 isolada: traje com status ALUGADO, demais VÁLIDAS")
        void ct10_deve_lancarBusinessException_quando_apenasTrajeNaoDisponivel() {
            AluguelRequest request = AlugueisDataBuilder.umAluguel().buildRequest();
            Traje indisponivel = AlugueisDataBuilder.umTrajeIndisponivel(TRAJE_ID_DEFAULT);

            when(clienteRepository.findById(CLIENTE_ID_DEFAULT)).thenReturn(Optional.of(cliente));
            when(trajeRepository.findById(TRAJE_ID_DEFAULT)).thenReturn(Optional.of(indisponivel));

            BusinessException ex = assertThrows(BusinessException.class, () -> service.criar(request));
            assertEquals("Traje não está disponível", ex.getMessage());
            verify(aluguelRepository, never()).save(any(Aluguel.class));
        }

        @Test
        @DisplayName("CT11 — I6 isolada: traje ocupado no período, demais VÁLIDAS")
        void ct11_deve_lancarBusinessException_quando_apenasTrajeOcupadoNoPeriodo() {
            AluguelRequest request = AlugueisDataBuilder.umAluguel().buildRequest();
            when(clienteRepository.findById(CLIENTE_ID_DEFAULT)).thenReturn(Optional.of(cliente));
            when(trajeRepository.findById(TRAJE_ID_DEFAULT)).thenReturn(Optional.of(traje));
            when(itemAluguelRepository.trajeIndisponivelNoPeriodo(
                    eq(TRAJE_ID_DEFAULT), any(LocalDate.class), any(LocalDate.class), eq(null)))
                    .thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () -> service.criar(request));
            assertEquals("Traje já está alugado nesse período", ex.getMessage());
            verify(aluguelRepository, never()).save(any(Aluguel.class));
        }

        @Test
        @DisplayName("CT12 — I7 isolada na borda: desconto = total + 0,01, demais VÁLIDAS")
        void ct12_deve_lancarBusinessException_quando_apenasDescontoUmCentavoAcimaDoTotal() {
            AluguelRequest request = AlugueisDataBuilder.umAluguel()
                    .comValorDesconto(new BigDecimal("100.01"))
                    .buildRequest();
            stubarCaminhoFelizCriar();

            BusinessException ex = assertThrows(BusinessException.class, () -> service.criar(request));
            assertEquals("O valor com desconto não pode ser negativo", ex.getMessage());
            verify(aluguelRepository, never()).save(any(Aluguel.class));
        }
    }

    // =========================================================
    // ATUALIZAR — CT13..CT18
    // =========================================================
    @Nested
    @DisplayName("Atualizar Aluguel — matriz TFS")
    class Atualizar {

        private Aluguel aluguelAtivo;

        @BeforeEach
        void setUpAtualizar() {
            aluguelAtivo = AlugueisDataBuilder.umAluguel()
                    .comStatus(StatusAluguel.ATIVO)
                    .buildEntity(cliente);
        }

        private void stubarCaminhoFelizAtualizar() {
            when(aluguelRepository.findById(ALUGUEL_ID_DEFAULT)).thenReturn(Optional.of(aluguelAtivo));
            when(itemAluguelRepository.trajeIndisponivelNoPeriodo(
                    eq(TRAJE_ID_DEFAULT), any(LocalDate.class), any(LocalDate.class), eq(ALUGUEL_ID_DEFAULT)))
                    .thenReturn(false);
            when(trajeRepository.findById(TRAJE_ID_DEFAULT)).thenReturn(Optional.of(traje));
        }

        @Test
        @DisplayName("CT13 — todas VÁLIDAS: aluguel ATIVO, datas/traje/desconto ok")
        void ct13_deve_atualizar_quando_todasClassesValidas() {
            // aluguelAtivo nasce com 1 item pré-existente, datas antigas e observação antiga,
            // de modo que cada efeito do método seja observável (mapper, clear, setValorTotal).
            aluguelAtivo = AlugueisDataBuilder.umAluguel()
                    .comStatus(StatusAluguel.ATIVO)
                    .comDatas(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2))
                    .comObservacoes("observacao antiga")
                    .buildEntityComItens(cliente, List.of(traje));

            LocalDate novaRetirada = LocalDate.now().plusDays(10);
            LocalDate novaDevolucao = LocalDate.now().plusDays(15);
            AluguelUpdateRequest request = AlugueisDataBuilder.umAluguel()
                    .comDatas(novaRetirada, novaDevolucao)
                    .comObservacoes("observacao nova")
                    .comValorDesconto(new BigDecimal("20.00"))
                    .buildUpdateRequest();
            stubarCaminhoFelizAtualizar();

            AluguelResponse response = service.atualizar(ALUGUEL_ID_DEFAULT, request);

            assertNotNull(response);
            // updateEntity precisa ter sido chamado: campos do DTO refletidos na resposta
            assertEquals(novaRetirada, response.dataRetirada());
            assertEquals(novaDevolucao, response.dataDevolucao());
            assertEquals("observacao nova", response.observacoes());
            // itens.clear() precisa ter sido chamado: sem ele teríamos 2 itens (1 antigo + 1 novo)
            assertEquals(1, response.itens().size());
            // setValorTotal precisa ter sido chamado: 100.00 - 20.00 = 80.00
            assertEquals(0, response.valorTotal().compareTo(new BigDecimal("80.00")));
            verify(aluguelRepository).save(any(Aluguel.class));
        }

        @Test
        @DisplayName("CT14 — I9 isolada: aluguel inexistente")
        void ct14_deve_lancarResourceNotFound_quando_apenasAluguelInexistente() {
            AluguelUpdateRequest request = AlugueisDataBuilder.umAluguel().buildUpdateRequest();
            when(aluguelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.atualizar(99L, request));
            verify(aluguelRepository, never()).save(any(Aluguel.class));
        }

        @Test
        @DisplayName("CT15 — I10 isolada: aluguel CONCLUÍDO (status ≠ ATIVO)")
        void ct15_deve_lancarBusinessException_quando_apenasStatusNaoAtivo() {
            Aluguel concluido = AlugueisDataBuilder.umAluguel()
                    .comStatus(StatusAluguel.CONCLUIDO)
                    .buildEntity(cliente);
            AluguelUpdateRequest request = AlugueisDataBuilder.umAluguel().buildUpdateRequest();
            when(aluguelRepository.findById(ALUGUEL_ID_DEFAULT)).thenReturn(Optional.of(concluido));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.atualizar(ALUGUEL_ID_DEFAULT, request));
            assertEquals("Só é possível alterar aluguéis ATIVOS ou EM ATRASO", ex.getMessage());
            verify(aluguelRepository, never()).save(any(Aluguel.class));
        }

        @Test
        @DisplayName("CT16 — I3 isolada na borda: devolução = retirada - 1")
        void ct16_deve_lancarBusinessException_quando_apenasDevolucaoAntesDaRetirada() {
            LocalDate retirada = LocalDate.now().plusDays(5);
            AluguelUpdateRequest request = AlugueisDataBuilder.umAluguel()
                    .comDatas(retirada, retirada.minusDays(1))
                    .buildUpdateRequest();
            when(aluguelRepository.findById(ALUGUEL_ID_DEFAULT)).thenReturn(Optional.of(aluguelAtivo));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.atualizar(ALUGUEL_ID_DEFAULT, request));
            assertEquals("A data de devolução deve ser após a data de retirada", ex.getMessage());
        }

        @Test
        @DisplayName("CT17 — I6 isolada: traje ocupado no novo período")
        void ct17_deve_lancarBusinessException_quando_apenasTrajeOcupadoNoPeriodo() {
            AluguelUpdateRequest request = AlugueisDataBuilder.umAluguel().buildUpdateRequest();
            when(aluguelRepository.findById(ALUGUEL_ID_DEFAULT)).thenReturn(Optional.of(aluguelAtivo));
            when(itemAluguelRepository.trajeIndisponivelNoPeriodo(
                    eq(TRAJE_ID_DEFAULT), any(LocalDate.class), any(LocalDate.class), eq(ALUGUEL_ID_DEFAULT)))
                    .thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.atualizar(ALUGUEL_ID_DEFAULT, request));
            assertEquals("Traje já está alugado nesse período", ex.getMessage());
            verify(aluguelRepository, never()).save(any(Aluguel.class));
        }

        @Test
        @DisplayName("CT18 — I7 isolada na borda: desconto = total + 0,01")
        void ct18_deve_lancarBusinessException_quando_apenasDescontoUmCentavoAcimaDoTotal() {
            AluguelUpdateRequest request = AlugueisDataBuilder.umAluguel()
                    .comValorDesconto(new BigDecimal("100.01"))
                    .buildUpdateRequest();
            stubarCaminhoFelizAtualizar();

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.atualizar(ALUGUEL_ID_DEFAULT, request));
            assertEquals("O valor com desconto não pode ser negativo", ex.getMessage());
            verify(aluguelRepository, never()).save(any(Aluguel.class));
        }

        @Test
        @DisplayName("CT25 — V7 borda: desconto=null no update (tratado como zero)")
        void ct25_deve_atualizar_quando_descontoNulo() {
            AluguelUpdateRequest request = AlugueisDataBuilder.umAluguel()
                    .comDescontoNulo()
                    .buildUpdateRequest();
            stubarCaminhoFelizAtualizar();

            AluguelResponse response = service.atualizar(ALUGUEL_ID_DEFAULT, request);

            assertNotNull(response);
            assertEquals(0, response.valorTotal().compareTo(new BigDecimal("100.00")));
        }
    }

    // =========================================================
    // BUSCAR POR ID — CT19, CT20
    // =========================================================
    @Nested
    @DisplayName("Buscar por ID — matriz TFS")
    class BuscarPorId {

        @Test
        @DisplayName("CT19 — V: ID existe")
        void ct19_deve_retornar_quando_idExiste() {
            Aluguel aluguel = AlugueisDataBuilder.umAluguel().buildEntity(cliente);
            when(aluguelRepository.findById(ALUGUEL_ID_DEFAULT)).thenReturn(Optional.of(aluguel));

            AluguelResponse response = service.buscarPorId(ALUGUEL_ID_DEFAULT);

            assertNotNull(response);
            assertEquals(ALUGUEL_ID_DEFAULT, response.id());
        }

        @Test
        @DisplayName("CT20 — I: ID inexistente")
        void ct20_deve_lancarResourceNotFound_quando_idInexistente() {
            when(aluguelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId(99L));
        }
    }

    // =========================================================
    // LISTAR TODOS — CT21, CT22
    // =========================================================
    @Nested
    @DisplayName("Listar Todos — matriz TFS")
    class ListarTodos {

        @Test
        @DisplayName("CT21 — V típico: existe pelo menos 1 aluguel")
        void ct21_deve_retornarLista_quando_existemAlugueis() {
            Aluguel aluguel = AlugueisDataBuilder.umAluguel().buildEntity(cliente);
            when(aluguelRepository.findAll()).thenReturn(List.of(aluguel));

            List<AluguelResponse> responses = service.listarTodos();

            assertEquals(1, responses.size());
        }

        @Test
        @DisplayName("CT22 — V borda inferior: nenhum aluguel cadastrado")
        void ct22_deve_retornarListaVazia_quando_nenhumAluguel() {
            when(aluguelRepository.findAll()).thenReturn(List.of());

            List<AluguelResponse> responses = service.listarTodos();

            assertTrue(responses.isEmpty());
        }
    }

    // =========================================================
    // DELETAR — CT23, CT24
    // =========================================================
    @Nested
    @DisplayName("Deletar — matriz TFS")
    class Deletar {

        @Test
        @DisplayName("CT23 — V: ID existe → deleta")
        void ct23_deve_deletar_quando_idExiste() {
            Aluguel aluguel = AlugueisDataBuilder.umAluguel().buildEntity(cliente);
            when(aluguelRepository.findById(ALUGUEL_ID_DEFAULT)).thenReturn(Optional.of(aluguel));

            service.deletar(ALUGUEL_ID_DEFAULT);

            verify(aluguelRepository).delete(aluguel);
        }

        @Test
        @DisplayName("CT24 — I: ID inexistente → nada é removido")
        void ct24_deve_lancarResourceNotFound_quando_idInexistente() {
            when(aluguelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.deletar(99L));
            verify(aluguelRepository, never()).delete(any(Aluguel.class));
        }
    }

    // =========================================================
    // LISTAR COM FILTROS — CT26..CT28
    // =========================================================
    @Nested
    @DisplayName("Listar com filtros — matriz TFS")
    class ListarComFiltros {

        @Test
        @DisplayName("CT26 — V típico: todos os filtros preenchidos compõem Specification e retornam lista mapeada")
        @SuppressWarnings("unchecked")
        void ct26_deve_listar_quando_todosFiltrosPreenchidos() {
            AluguelFiltroRequest filtro = new AluguelFiltroRequest(
                    StatusAluguel.ATIVO,
                    "Maria",
                    LocalDate.now(),
                    LocalDate.now().plusDays(10),
                    TipoOcasiao.CASAMENTO);
            Aluguel a1 = AlugueisDataBuilder.umAluguel().comId(1L).buildEntity(cliente);
            Aluguel a2 = AlugueisDataBuilder.umAluguel().comId(2L).buildEntity(cliente);
            when(aluguelRepository.findAll(any(Specification.class))).thenReturn(List.of(a1, a2));

            List<AluguelResponse> responses = service.listarComFiltros(filtro);

            // mata mutante linha 174 (return Collections.emptyList): emptyList tem size==0
            assertEquals(2, responses.size());
            assertEquals(1L, responses.get(0).id());
            assertEquals(2L, responses.get(1).id());
        }

        @Test
        @DisplayName("CT27 — V parcial: somente status preenchido")
        @SuppressWarnings("unchecked")
        void ct27_deve_listar_quando_apenasStatus() {
            AluguelFiltroRequest filtro = new AluguelFiltroRequest(
                    StatusAluguel.ATIVO, null, null, null, null);
            when(aluguelRepository.findAll(any(Specification.class)))
                    .thenReturn(List.of(AlugueisDataBuilder.umAluguel().buildEntity(cliente)));

            List<AluguelResponse> responses = service.listarComFiltros(filtro);

            assertEquals(1, responses.size());
        }

        @Test
        @DisplayName("CT28 — V borda: filtro totalmente nulo")
        @SuppressWarnings("unchecked")
        void ct28_deve_listar_quando_filtroTodoNulo() {
            AluguelFiltroRequest filtro = new AluguelFiltroRequest(null, null, null, null, null);
            when(aluguelRepository.findAll(any(Specification.class))).thenReturn(List.of());

            List<AluguelResponse> responses = service.listarComFiltros(filtro);

            assertTrue(responses.isEmpty());
        }
    }

    // =========================================================
    // BUSCAR ATIVO POR TRAJE — CT29..CT30
    // =========================================================
    @Nested
    @DisplayName("Buscar aluguel ATIVO por traje — matriz TFS")
    class BuscarAtivoByTrajeId {

        @Test
        @DisplayName("CT29 — V: existe item ATIVO para o traje")
        void ct29_deve_retornarResponse_quando_existeItemAtivo() {
            Aluguel aluguel = AlugueisDataBuilder.umAluguel().buildEntity(cliente);
            ItemAluguel item = ItemAluguel.builder()
                    .id(500L)
                    .aluguel(aluguel)
                    .traje(traje)
                    .build();
            when(itemAluguelRepository.findAtivoByTrajeId(TRAJE_ID_DEFAULT))
                    .thenReturn(Optional.of(item));

            AluguelResponse response = service.buscarAtivoByTrajeId(TRAJE_ID_DEFAULT);

            // mata mutante linha 187 (return null)
            assertNotNull(response);
            assertEquals(ALUGUEL_ID_DEFAULT, response.id());
        }

        @Test
        @DisplayName("CT30 — I: não existe item ATIVO → ResourceNotFoundException")
        void ct30_deve_lancarResourceNotFound_quando_naoExisteItemAtivo() {
            when(itemAluguelRepository.findAtivoByTrajeId(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.buscarAtivoByTrajeId(99L));
            // mata mutante linha 186 (lambda → null): valida que a exceção foi a do orElseThrow
            assertTrue(ex.getMessage().contains("Aluguel ativo para o traje"));
        }
    }

    // =========================================================
    // REGISTRAR DEVOLUCAO — CT31..CT34
    // =========================================================
    @Nested
    @DisplayName("Registrar devolução — matriz TFS")
    class RegistrarDevolucao {

        private Aluguel ativo;

        @BeforeEach
        void setUpAtivo() {
            ativo = AlugueisDataBuilder.umAluguel()
                    .comStatus(StatusAluguel.ATIVO)
                    .buildEntity(cliente);
        }

        @Test
        @DisplayName("CT31 — V típico: aluguel ATIVO + itens preenchidos → trajes atualizados, aluguel CONCLUIDO")
        void ct31_deve_registrarDevolucao_quando_caminhoFeliz() {
            DevolucaoRequest dto = new DevolucaoRequest(
                    LocalDate.now(),
                    "obs",
                    BigDecimal.ZERO,
                    List.of(new ItemDevolucaoRequest(TRAJE_ID_DEFAULT, CondicaoTraje.BOM)));
            DevolucaoResponse stubResponse = new DevolucaoResponse(
                    1L, LocalDate.now(), "obs", BigDecimal.ZERO, ALUGUEL_ID_DEFAULT);
            // Traje começa ALUGADO de propósito: caso o setStatus(DISPONIVEL) seja
            // removido (mutante linha 216), o status final permaneceria ALUGADO.
            Traje trajeAlugado = AlugueisDataBuilder.umTrajeIndisponivel(TRAJE_ID_DEFAULT);
            when(aluguelRepository.findById(ALUGUEL_ID_DEFAULT)).thenReturn(Optional.of(ativo));
            when(trajeRepository.findById(TRAJE_ID_DEFAULT)).thenReturn(Optional.of(trajeAlugado));
            when(devolucaoService.criar(eq(dto), eq(ativo))).thenReturn(stubResponse);

            DevolucaoResponse response = service.registrarDevolucao(ALUGUEL_ID_DEFAULT, dto);

            // mata mutante linha 226 (return null): a resposta é exatamente a do devolucaoService
            assertEquals(stubResponse, response);

            // mata mutantes linhas 213/215/216: forEach roda e mexe no Traje
            ArgumentCaptor<Traje> trajeCaptor = ArgumentCaptor.forClass(Traje.class);
            verify(trajeRepository).save(trajeCaptor.capture());
            assertEquals(CondicaoTraje.BOM, trajeCaptor.getValue().getCondicao());
            assertEquals(StatusTraje.DISPONIVEL, trajeCaptor.getValue().getStatus());

            // mata mutante linha 223: aluguel salvo com status CONCLUIDO
            ArgumentCaptor<Aluguel> aluguelCaptor = ArgumentCaptor.forClass(Aluguel.class);
            verify(aluguelRepository).save(aluguelCaptor.capture());
            assertEquals(StatusAluguel.CONCLUIDO, aluguelCaptor.getValue().getStatus());
        }

        @Test
        @DisplayName("CT31b — multa na devolução deve ser somada ao valor total do aluguel")
        void ct31b_deve_somar_multa_ao_valor_total_na_devolucao() {
            ativo.setValorTotal(new BigDecimal("450.00"));
            BigDecimal multa = new BigDecimal("50.00");
            DevolucaoRequest dto = new DevolucaoRequest(
                    LocalDate.now(),
                    null,
                    multa,
                    List.of(new ItemDevolucaoRequest(TRAJE_ID_DEFAULT, CondicaoTraje.BOM)));
            DevolucaoResponse stubResponse = new DevolucaoResponse(
                    1L, LocalDate.now(), null, multa, ALUGUEL_ID_DEFAULT);
            when(aluguelRepository.findById(ALUGUEL_ID_DEFAULT)).thenReturn(Optional.of(ativo));
            when(trajeRepository.findById(TRAJE_ID_DEFAULT))
                    .thenReturn(Optional.of(AlugueisDataBuilder.umTrajeDisponivel(TRAJE_ID_DEFAULT)));
            when(devolucaoService.criar(eq(dto), eq(ativo))).thenReturn(stubResponse);

            service.registrarDevolucao(ALUGUEL_ID_DEFAULT, dto);

            ArgumentCaptor<Aluguel> aluguelCaptor = ArgumentCaptor.forClass(Aluguel.class);
            verify(aluguelRepository).save(aluguelCaptor.capture());
            assertEquals(0, new BigDecimal("500.00").compareTo(aluguelCaptor.getValue().getValorTotal()));
            assertEquals(0, multa.compareTo(aluguelCaptor.getValue().getValorMulta()));
            assertEquals(StatusAluguel.CONCLUIDO, aluguelCaptor.getValue().getStatus());
        }

        @Test
        @DisplayName("CT32 — V borda: aluguel ATIVO + itens=null → não atualiza trajes, mas conclui aluguel")
        void ct32_deve_registrarDevolucao_quando_itensNulos() {
            DevolucaoRequest dto = new DevolucaoRequest(
                    LocalDate.now(), null, null, null);
            DevolucaoResponse stubResponse = new DevolucaoResponse(
                    2L, LocalDate.now(), null, null, ALUGUEL_ID_DEFAULT);
            when(aluguelRepository.findById(ALUGUEL_ID_DEFAULT)).thenReturn(Optional.of(ativo));
            when(devolucaoService.criar(eq(dto), eq(ativo))).thenReturn(stubResponse);

            service.registrarDevolucao(ALUGUEL_ID_DEFAULT, dto);

            // mata mutante linha 212 (== true): se a guarda fosse sempre true, dto.itens().forEach lançaria NPE
            verify(trajeRepository, never()).save(any(Traje.class));

            // status CONCLUIDO mesmo sem itens (mata mutante linha 223 também)
            ArgumentCaptor<Aluguel> aluguelCaptor = ArgumentCaptor.forClass(Aluguel.class);
            verify(aluguelRepository).save(aluguelCaptor.capture());
            assertEquals(StatusAluguel.CONCLUIDO, aluguelCaptor.getValue().getStatus());
        }

        @Test
        @DisplayName("CT33 — I: aluguel inexistente → ResourceNotFoundException")
        void ct33_deve_lancarResourceNotFound_quando_aluguelInexistente() {
            DevolucaoRequest dto = new DevolucaoRequest(LocalDate.now(), null, null, null);
            when(aluguelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.registrarDevolucao(99L, dto));
            verify(aluguelRepository, never()).save(any(Aluguel.class));
            verify(devolucaoService, never()).criar(any(), any());
        }

        @Test
        @DisplayName("CT34 — I: aluguel CONCLUÍDO → BusinessException")
        void ct34_deve_lancarBusinessException_quando_aluguelNaoAtivo() {
            Aluguel concluido = AlugueisDataBuilder.umAluguel()
                    .comStatus(StatusAluguel.CONCLUIDO)
                    .buildEntity(cliente);
            DevolucaoRequest dto = new DevolucaoRequest(LocalDate.now(), null, null, null);
            when(aluguelRepository.findById(ALUGUEL_ID_DEFAULT)).thenReturn(Optional.of(concluido));

            // mata mutante linha 207 (path I do equality check): só ATIVOS lançam BusinessException
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.registrarDevolucao(ALUGUEL_ID_DEFAULT, dto));
            assertTrue(ex.getMessage().contains("ATIVOS") || ex.getMessage().contains("ATRASO"));
            verify(aluguelRepository, never()).save(any(Aluguel.class));
            verify(devolucaoService, never()).criar(any(), any());
        }

        @Test
        @DisplayName("CT35 — V: aluguel EM ATRASO pode registrar devolução com multa")
        void ct35_deve_registrar_devolucao_quando_aluguel_em_atraso() {
            Aluguel emAtraso = AlugueisDataBuilder.umAluguel()
                    .comStatus(StatusAluguel.ATRASO)
                    .buildEntity(cliente);
            emAtraso.setValorTotal(new BigDecimal("300.00"));
            DevolucaoRequest dto = new DevolucaoRequest(
                    LocalDate.now(), null, new BigDecimal("30.00"), null);
            when(aluguelRepository.findById(ALUGUEL_ID_DEFAULT)).thenReturn(Optional.of(emAtraso));
            when(devolucaoService.criar(eq(dto), eq(emAtraso)))
                    .thenReturn(new DevolucaoResponse(1L, LocalDate.now(), null, new BigDecimal("30.00"), ALUGUEL_ID_DEFAULT));

            service.registrarDevolucao(ALUGUEL_ID_DEFAULT, dto);

            ArgumentCaptor<Aluguel> aluguelCaptor = ArgumentCaptor.forClass(Aluguel.class);
            verify(aluguelRepository).save(aluguelCaptor.capture());
            assertEquals(0, new BigDecimal("330.00").compareTo(aluguelCaptor.getValue().getValorTotal()));
            assertEquals(0, new BigDecimal("30.00").compareTo(aluguelCaptor.getValue().getValorMulta()));
        }
    }
}
