package br.edu.fateczl.tcc.controller;

import br.edu.fateczl.tcc.dto.aluguel.AluguelRequest;
import br.edu.fateczl.tcc.dto.aluguel.AluguelResponse;
import br.edu.fateczl.tcc.dto.aluguel.AluguelUpdateRequest;
import br.edu.fateczl.tcc.dto.aluguel.ItemAluguelResponse;
import br.edu.fateczl.tcc.dto.devolucao.DevolucaoRequest;
import br.edu.fateczl.tcc.dto.devolucao.DevolucaoResponse;
import br.edu.fateczl.tcc.dto.devolucao.ItemDevolucaoRequest;
import br.edu.fateczl.tcc.enums.CondicaoTraje;
import br.edu.fateczl.tcc.enums.CorTraje;
import br.edu.fateczl.tcc.enums.StatusAluguel;
import br.edu.fateczl.tcc.enums.TamanhoTraje;
import br.edu.fateczl.tcc.enums.TipoOcasiao;
import br.edu.fateczl.tcc.enums.TipoTraje;
import br.edu.fateczl.tcc.exception.BusinessException;
import br.edu.fateczl.tcc.exception.ResourceNotFoundException;
import br.edu.fateczl.tcc.service.AluguelService;
import br.edu.fateczl.tcc.service.ContratoPdfService;
import br.edu.fateczl.tcc.util.AlugueisDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AluguelController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes unitários do AluguelController")
class AluguelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AluguelService service;

    @MockitoBean
    private ContratoPdfService contratoPdfService;

    @Autowired
    private ObjectMapper objectMapper;

    private AluguelRequest requestValido;
    private AluguelUpdateRequest updateRequestValido;
    private AluguelResponse responseValido;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        requestValido = AlugueisDataBuilder.umAluguel().buildRequest();
        updateRequestValido = AlugueisDataBuilder.umAluguel().buildUpdateRequest();
        responseValido = montarResponse();
    }

    private AluguelResponse montarResponse() {
        return new AluguelResponse(
                AlugueisDataBuilder.ALUGUEL_ID_DEFAULT,
                AlugueisDataBuilder.CLIENTE_ID_DEFAULT,
                "Cliente Teste 1",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(7),
                AlugueisDataBuilder.VALOR_TRAJE_DEFAULT,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "Observacao de teste",
                StatusAluguel.ATIVO,
                TipoOcasiao.FORMATURA,
                List.of(new ItemAluguelResponse(AlugueisDataBuilder.TRAJE_ID_DEFAULT, "Traje 10", TipoTraje.TERNO, TamanhoTraje.M, CorTraje.PRETO, AlugueisDataBuilder.VALOR_TRAJE_DEFAULT))
        );
    }

    @Nested
    @DisplayName("Criar Aluguel")
    class CriarAluguelTest {

        @Test
        void deve_retornar201_quando_aluguelCriadoComSucesso() throws Exception {
            when(service.criar(any(AluguelRequest.class))).thenReturn(responseValido);

            mockMvc.perform(post("/alugueis")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestValido)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT))
                    .andExpect(jsonPath("$.clienteId").value(AlugueisDataBuilder.CLIENTE_ID_DEFAULT))
                    .andExpect(jsonPath("$.itens.length()").value(1));

            verify(service).criar(any(AluguelRequest.class));
        }

        @Test
        void deve_retornar400_quando_clienteIdNulo() throws Exception {
            AluguelRequest request = AlugueisDataBuilder.umAluguel()
                    .comClienteId(null)
                    .buildRequest();

            mockMvc.perform(post("/alugueis")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(service, never()).criar(any(AluguelRequest.class));
        }

        @Test
        void deve_retornar400_quando_dataRetiradaNula() throws Exception {
            AluguelRequest request = AlugueisDataBuilder.umAluguel()
                    .comDataRetirada(null)
                    .buildRequest();

            mockMvc.perform(post("/alugueis")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(service, never()).criar(any(AluguelRequest.class));
        }

        @Test
        void deve_retornar400_quando_dataDevolucaoNula() throws Exception {
            AluguelRequest request = AlugueisDataBuilder.umAluguel()
                    .comDataDevolucao(null)
                    .buildRequest();

            mockMvc.perform(post("/alugueis")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(service, never()).criar(any(AluguelRequest.class));
        }

        @Test
        void deve_retornar400_quando_itensVazios() throws Exception {
            AluguelRequest request = AlugueisDataBuilder.umAluguel()
                    .semItens()
                    .buildRequest();

            mockMvc.perform(post("/alugueis")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(service, never()).criar(any(AluguelRequest.class));
        }

        @Test
        void deve_retornar400_quando_valorDescontoNegativo() throws Exception {
            AluguelRequest request = AlugueisDataBuilder.umAluguel()
                    .comValorDesconto(new BigDecimal("-10.00"))
                    .buildRequest();

            mockMvc.perform(post("/alugueis")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(service, never()).criar(any(AluguelRequest.class));
        }

        @Test
        void deve_retornar404_quando_clienteNaoEncontrado() throws Exception {
            when(service.criar(any(AluguelRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Cliente", 99L));

            mockMvc.perform(post("/alugueis")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestValido)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Cliente com id 99 não encontrado(a)"));
        }

        @Test
        void deve_retornar404_quando_trajeNaoEncontrado() throws Exception {
            when(service.criar(any(AluguelRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Traje", 99L));

            mockMvc.perform(post("/alugueis")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestValido)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Traje com id 99 não encontrado(a)"));
        }

        @Test
        void deve_retornar400_quando_trajeIndisponivel() throws Exception {
            when(service.criar(any(AluguelRequest.class)))
                    .thenThrow(new BusinessException("Traje não está disponível"));

            mockMvc.perform(post("/alugueis")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestValido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Traje não está disponível"));
        }

        @Test
        void deve_retornar400_quando_dataRetiradaNoPassado() throws Exception {
            when(service.criar(any(AluguelRequest.class)))
                    .thenThrow(new BusinessException("A data de retirada não pode ser no passado"));

            mockMvc.perform(post("/alugueis")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestValido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("A data de retirada não pode ser no passado"));
        }

        @Test
        void deve_retornar400_quando_descontoMaiorQueTotal() throws Exception {
            when(service.criar(any(AluguelRequest.class)))
                    .thenThrow(new BusinessException("O valor com desconto não pode ser negativo"));

            mockMvc.perform(post("/alugueis")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestValido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("O valor com desconto não pode ser negativo"));
        }

        @Test
        void deve_retornar400_quando_devolucaoAntesDeRetirada() throws Exception {
            when(service.criar(any(AluguelRequest.class)))
                    .thenThrow(new BusinessException("A data de devolução deve ser após a data de retirada"));

            mockMvc.perform(post("/alugueis")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestValido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("A data de devolução deve ser após a data de retirada"));
        }

        @Test
        void deve_retornar400_quando_camposObrigatoriosNulos() throws Exception {
            String requestInvalido = """
                    {
                        "clienteId": null,
                        "dataRetirada": null,
                        "dataDevolucao": null,
                        "valorDesconto": null,
                        "observacoes": null,
                        "ocasiao": null,
                        "itens": []
                    }
                    """;

            mockMvc.perform(post("/alugueis")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestInvalido))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Buscar por ID")
    class BuscarPorIdTest {

        @Test
        void deve_retornar200_quando_aluguelEncontrado() throws Exception {
            when(service.buscarPorId(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT)).thenReturn(responseValido);

            mockMvc.perform(get("/alugueis/{id}", AlugueisDataBuilder.ALUGUEL_ID_DEFAULT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT))
                    .andExpect(jsonPath("$.clienteId").value(AlugueisDataBuilder.CLIENTE_ID_DEFAULT));

            verify(service).buscarPorId(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT);
        }

        @Test
        void deve_retornar404_quando_aluguelNaoEncontrado() throws Exception {
            when(service.buscarPorId(99L))
                    .thenThrow(new ResourceNotFoundException("Aluguel", 99L));

            mockMvc.perform(get("/alugueis/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Aluguel com id 99 não encontrado(a)"));
        }
    }

    @Nested
    @DisplayName("Listar Aluguéis")
    class ListarAlugueisTest {

        @Test
        void deve_retornar200_quando_listarTodos() throws Exception {
            when(service.listarComFiltros(any())).thenReturn(List.of(responseValido));

            mockMvc.perform(get("/alugueis"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT))
                    .andExpect(jsonPath("$[0].clienteId").value(AlugueisDataBuilder.CLIENTE_ID_DEFAULT));

            verify(service).listarComFiltros(any());
        }

        @Test
        void deve_retornar200_quando_listaVazia() throws Exception {
            when(service.listarComFiltros(any())).thenReturn(List.of());

            mockMvc.perform(get("/alugueis"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(service).listarComFiltros(any());
        }
    }

    @Nested
    @DisplayName("Atualizar Aluguel")
    class AtualizarAluguelTest {

        @Test
        void deve_retornar200_quando_atualizadoComSucesso() throws Exception {
            when(service.atualizar(eq(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT), any(AluguelUpdateRequest.class)))
                    .thenReturn(responseValido);

            mockMvc.perform(put("/alugueis/{id}", AlugueisDataBuilder.ALUGUEL_ID_DEFAULT)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequestValido)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT));

            verify(service).atualizar(eq(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT), any(AluguelUpdateRequest.class));
        }

        @Test
        void deve_retornar400_quando_statusNuloNaAtualizacao() throws Exception {
            AluguelUpdateRequest request = AlugueisDataBuilder.umAluguel()
                    .comStatus(null)
                    .buildUpdateRequest();

            mockMvc.perform(put("/alugueis/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(service, never()).atualizar(any(), any(AluguelUpdateRequest.class));
        }

        @Test
        void deve_retornar400_quando_dataRetiradaNoPassadoNaAtualizacao() throws Exception {
            AluguelUpdateRequest request = AlugueisDataBuilder.umAluguel()
                    .comDataRetirada(LocalDate.now().minusDays(1))
                    .buildUpdateRequest();

            mockMvc.perform(put("/alugueis/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(service, never()).atualizar(any(), any(AluguelUpdateRequest.class));
        }

        @Test
        void deve_retornar400_quando_itensVaziosNaAtualizacao() throws Exception {
            AluguelUpdateRequest request = AlugueisDataBuilder.umAluguel()
                    .semItens()
                    .buildUpdateRequest();

            mockMvc.perform(put("/alugueis/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(service, never()).atualizar(any(), any(AluguelUpdateRequest.class));
        }

        @Test
        void deve_retornar404_quando_aluguelNaoEncontradoParaAtualizar() throws Exception {
            when(service.atualizar(eq(99L), any(AluguelUpdateRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Aluguel", 99L));

            mockMvc.perform(put("/alugueis/99")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequestValido)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Aluguel com id 99 não encontrado(a)"));
        }

        @Test
        void deve_retornar400_quando_aluguelNaoEstaAtivo() throws Exception {
            when(service.atualizar(eq(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT), any(AluguelUpdateRequest.class)))
                    .thenThrow(new BusinessException("Só é possível alterar aluguéis ATIVOS ou EM ATRASO"));

            mockMvc.perform(put("/alugueis/{id}", AlugueisDataBuilder.ALUGUEL_ID_DEFAULT)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequestValido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Só é possível alterar aluguéis ATIVOS ou EM ATRASO"));
        }

        @Test
        void deve_retornar400_quando_trajeJaAlugadoNoPeriodo() throws Exception {
            when(service.atualizar(eq(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT), any(AluguelUpdateRequest.class)))
                    .thenThrow(new BusinessException("Traje já está alugado nesse período"));

            mockMvc.perform(put("/alugueis/{id}", AlugueisDataBuilder.ALUGUEL_ID_DEFAULT)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequestValido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Traje já está alugado nesse período"));
        }
    }

    @Nested
    @DisplayName("Deletar Aluguel")
    class DeletarAluguelTest {

        @Test
        void deve_retornar204_quando_deletarComSucesso() throws Exception {
            doNothing().when(service).deletar(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT);

            mockMvc.perform(delete("/alugueis/{id}", AlugueisDataBuilder.ALUGUEL_ID_DEFAULT)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(service).deletar(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT);
        }

        @Test
        void deve_retornar404_quando_aluguelNaoEncontradoParaDeletar() throws Exception {
            doThrow(new ResourceNotFoundException("Aluguel", 99L))
                    .when(service).deletar(99L);

            mockMvc.perform(delete("/alugueis/99")
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Aluguel com id 99 não encontrado(a)"));
        }
    }

    @Nested
    @DisplayName("Gerar Contrato em PDF")
    class GerarContratoTest {

        @Test
        void deve_retornar200ComPdf_quando_aluguelExiste() throws Exception {
            byte[] pdfBytes = "%PDF-1.4 fake".getBytes();
            when(contratoPdfService.gerarContrato(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT))
                    .thenReturn(pdfBytes);

            mockMvc.perform(get("/alugueis/{id}/contrato", AlugueisDataBuilder.ALUGUEL_ID_DEFAULT))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(header().string("Content-Disposition",
                            "inline; filename=\"Contrato_Aluguel_" + AlugueisDataBuilder.ALUGUEL_ID_DEFAULT + ".pdf\""))
                    .andExpect(content().bytes(pdfBytes));

            verify(contratoPdfService).gerarContrato(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT);
        }

        @Test
        void deve_retornar404_quando_aluguelNaoEncontradoParaContrato() throws Exception {
            when(contratoPdfService.gerarContrato(99L))
                    .thenThrow(new ResourceNotFoundException("Aluguel", 99L));

            mockMvc.perform(get("/alugueis/99/contrato"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Aluguel com id 99 não encontrado(a)"));
        }
    }

    @Nested
    @DisplayName("Registrar Devolução")
    class RegistrarDevolucaoTest {

        private DevolucaoRequest devolucaoRequest() {
            return new DevolucaoRequest(
                    LocalDate.now(),
                    "Devolvido em bom estado",
                    BigDecimal.ZERO,
                    List.of(new ItemDevolucaoRequest(AlugueisDataBuilder.TRAJE_ID_DEFAULT, CondicaoTraje.BOM))
            );
        }

        private DevolucaoResponse devolucaoResponse() {
            return new DevolucaoResponse(
                    1L,
                    LocalDate.now(),
                    "Devolvido em bom estado",
                    BigDecimal.ZERO,
                    AlugueisDataBuilder.ALUGUEL_ID_DEFAULT
            );
        }

        @Test
        void deve_retornar201_quando_registrarDevolucaoComSucesso() throws Exception {
            when(service.registrarDevolucao(eq(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT), any(DevolucaoRequest.class)))
                    .thenReturn(devolucaoResponse());

            mockMvc.perform(post("/alugueis/{id}/devolucao", AlugueisDataBuilder.ALUGUEL_ID_DEFAULT)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(devolucaoRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.idAluguel").value(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT))
                    .andExpect(jsonPath("$.observacoes").value("Devolvido em bom estado"));

            verify(service).registrarDevolucao(eq(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT), any(DevolucaoRequest.class));
        }

        @Test
        void deve_retornar404_quando_aluguelNaoEncontradoParaDevolucao() throws Exception {
            when(service.registrarDevolucao(eq(99L), any(DevolucaoRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Aluguel", 99L));

            mockMvc.perform(post("/alugueis/99/devolucao")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(devolucaoRequest())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Aluguel com id 99 não encontrado(a)"));
        }

        @Test
        void deve_retornar400_quando_aluguelNaoEstaAtivo() throws Exception {
            when(service.registrarDevolucao(eq(AlugueisDataBuilder.ALUGUEL_ID_DEFAULT), any(DevolucaoRequest.class)))
                    .thenThrow(new BusinessException("Só é possível registrar devolução de aluguéis ATIVOS"));

            mockMvc.perform(post("/alugueis/{id}/devolucao", AlugueisDataBuilder.ALUGUEL_ID_DEFAULT)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(devolucaoRequest())))
                    .andExpect(status().isBadRequest());
        }
    }
}
