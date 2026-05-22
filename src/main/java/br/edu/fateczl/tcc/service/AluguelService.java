package br.edu.fateczl.tcc.service;

import br.edu.fateczl.tcc.domain.Aluguel;
import br.edu.fateczl.tcc.domain.Cliente;
import br.edu.fateczl.tcc.domain.ItemAluguel;
import br.edu.fateczl.tcc.domain.Traje;
import br.edu.fateczl.tcc.dto.aluguel.AluguelFiltroRequest;
import br.edu.fateczl.tcc.dto.aluguel.AluguelRequest;
import br.edu.fateczl.tcc.dto.aluguel.AluguelResponse;
import br.edu.fateczl.tcc.dto.aluguel.AluguelUpdateRequest;
import br.edu.fateczl.tcc.dto.aluguel.ItemAluguelRequest;
import br.edu.fateczl.tcc.dto.devolucao.DevolucaoRequest;
import br.edu.fateczl.tcc.dto.devolucao.DevolucaoResponse;
import br.edu.fateczl.tcc.specification.AluguelSpecification;
import org.springframework.data.jpa.domain.Specification;
import br.edu.fateczl.tcc.enums.StatusAluguel;
import br.edu.fateczl.tcc.enums.StatusTraje;
import br.edu.fateczl.tcc.exception.BusinessException;
import br.edu.fateczl.tcc.exception.ResourceNotFoundException;
import br.edu.fateczl.tcc.mapper.AluguelMapper;
import br.edu.fateczl.tcc.mapper.ItemAluguelMapper;
import br.edu.fateczl.tcc.repository.AluguelRepository;
import br.edu.fateczl.tcc.repository.ClienteRepository;
import br.edu.fateczl.tcc.repository.ItemAluguelRepository;
import br.edu.fateczl.tcc.repository.TrajeRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

@Service
public class AluguelService {

    private final AluguelRepository aluguelRepository;
    private final ClienteRepository clienteRepository;
    private final TrajeRepository trajeRepository;
    private final ItemAluguelRepository itemAluguelRepository;
    private final DevolucaoService devolucaoService;

    private static final String RESOURCE_ALUGUEL = "Aluguel";
    private static final String RESOURCE_CLIENTE = "Cliente";
    private static final String RESOURCE_TRAJE = "Traje";
    private static final EnumSet<StatusAluguel> STATUS_EM_ANDAMENTO =
            EnumSet.of(StatusAluguel.ATIVO, StatusAluguel.ATRASO);

    public AluguelService(AluguelRepository aluguelRepository,
                          ClienteRepository clienteRepository,
                          TrajeRepository trajeRepository,
                          ItemAluguelRepository itemAluguelRepository,
                          DevolucaoService devolucaoService) {
        this.aluguelRepository = aluguelRepository;
        this.clienteRepository = clienteRepository;
        this.trajeRepository = trajeRepository;
        this.itemAluguelRepository = itemAluguelRepository;
        this.devolucaoService = devolucaoService;
    }


    // ===============================
    // CREATE
    // ===============================
    @Transactional
    public AluguelResponse criar(AluguelRequest dto) {
        Cliente cliente = buscarClienteOuFalhar(dto.clienteId());
        validarDatas(dto.dataRetirada(), dto.dataDevolucao());

        // Criar aluguel (sem itens ainda)
        Aluguel aluguel = AluguelMapper.toEntity(dto, cliente);
        aluguel.setStatus(StatusAluguel.ATIVO);

        // Criar itens
        List<ItemAluguel> itens = criarItens(dto.itens(), aluguel, null);
        aluguel.getItens().addAll(itens);

        // Calcular valor total dos itens
        BigDecimal total = calcularValorTotal(itens);

        // Subtrair o valor do desconto (se houver)
        BigDecimal desconto = dto.valorDesconto() != null ? dto.valorDesconto() : BigDecimal.ZERO;
        BigDecimal valorComDesconto = total.subtract(desconto);
        aluguel.setValorTotal(valorComDesconto);
        aluguel.setValorMulta(BigDecimal.ZERO);

        // Verificar se o valor com desconto é negativo
        validarValorComDesconto(valorComDesconto);

        aluguelRepository.save(aluguel);
        return AluguelMapper.toResponse(aluguel);
    }


    // ===============================
    // UPDATE
    // ===============================
    @Transactional
    public AluguelResponse atualizar(Long id, AluguelUpdateRequest dto) {
        Aluguel aluguel = buscarAluguelOuFalhar(id);

        if (!STATUS_EM_ANDAMENTO.contains(aluguel.getStatus())) {
            throw new BusinessException("Só é possível alterar aluguéis ATIVOS ou EM ATRASO");
        }

        validarDatas(dto.dataRetirada(), dto.dataDevolucao());
        atualizarStatusConformePrazo(aluguel, dto.dataDevolucao());

        // Validar conflito com os NOVOS itens do DTO
        for (ItemAluguelRequest itemDto : dto.itens()) {
            validarDisponibilidadePeriodo(
                    itemDto.trajeId(),
                    dto.dataRetirada(),
                    dto.dataDevolucao(),
                    aluguel.getId()
            );
        }

        // Atualizar dados do aluguel (sem itens, para evitar quebra do orphan removal)
        AluguelMapper.updateEntity(aluguel, dto);

        // Criar novos itens e recalcular o valor total
        List<ItemAluguel> itensAtualizados = criarItens(dto.itens(), aluguel, aluguel.getId());

        // Atualizar a lista de itens no aluguel, removendo os órfãos
        aluguel.getItens().clear();  // Limpa os itens antigos
        aluguel.getItens().addAll(itensAtualizados);  // Adiciona os novos itens

        // Calcular o novo valor total
        BigDecimal total = calcularValorTotal(itensAtualizados);

        // Subtrair o valor do desconto (se houver)
        BigDecimal desconto = dto.valorDesconto() != null ? dto.valorDesconto() : BigDecimal.ZERO;
        BigDecimal valorComDesconto = total.subtract(desconto);

        // Verificar se o valor com desconto é negativo
        validarValorComDesconto(valorComDesconto);

        // Atualizar o valor total do aluguel
        aluguel.setValorTotal(valorComDesconto);

        aluguelRepository.save(aluguel);
        return AluguelMapper.toResponse(aluguel);
    }


    // ===============================
    // READ - por ID
    // ===============================
    @Transactional
    public AluguelResponse buscarPorId(Long id) {
        Aluguel aluguel = buscarAluguelOuFalhar(id);
        sincronizarStatusEmAtraso(aluguel);
        aluguelRepository.save(aluguel);
        return AluguelMapper.toResponse(aluguel);
    }


    // ===============================
    // READ - todos
    // ===============================
    @Transactional(readOnly = true)
    public List<AluguelResponse> listarTodos() {
        return aluguelRepository.findAll().stream()
                .map(AluguelMapper::toResponse)
                .toList();
    }


    // ===============================
    // READ - com filtros
    // ===============================
    @Transactional
    public List<AluguelResponse> listarComFiltros(AluguelFiltroRequest filtro) {
        sincronizarStatusEmAtraso();
        Specification<Aluguel> specification = Specification
                .where(AluguelSpecification.comStatus(filtro.status()))
                .and(AluguelSpecification.comNomeCliente(filtro.nomeCliente()))
                .and(AluguelSpecification.comDataRetiradaEntre(filtro.dataRetiradaInicio(), filtro.dataRetiradaFim()))
                .and(AluguelSpecification.comOcasiao(filtro.ocasiao()));

        return aluguelRepository.findAll(specification).stream()
                .map(AluguelMapper::toResponse)
                .toList();
    }


    // ===============================
    // READ - aluguel ativo por traje
    // ===============================
    @Transactional(readOnly = true)
    public AluguelResponse buscarAtivoByTrajeId(Long trajeId) {
        ItemAluguel item = itemAluguelRepository.findAtivoByTrajeId(trajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluguel ativo para o traje", trajeId));
        return AluguelMapper.toResponse(item.getAluguel());
    }


    // ===============================
    // DELETE
    // ===============================
    @Transactional
    public void deletar(Long id) {
        aluguelRepository.delete(buscarAluguelOuFalhar(id));
    }


    // ===============================
    // DEVOLUCAO
    // ===============================
    @Transactional
    public DevolucaoResponse registrarDevolucao(Long aluguelId, DevolucaoRequest dto) {
        Aluguel aluguel = buscarAluguelOuFalhar(aluguelId);

        if (!STATUS_EM_ANDAMENTO.contains(aluguel.getStatus())) {
            throw new BusinessException("Só é possível registrar devolução de aluguéis ATIVOS ou EM ATRASO");
        }

        // Atualizar a condição de cada traje informada na devolução
        if (dto.itens() != null) {
            dto.itens().forEach(itemDto -> {
                Traje traje = buscarTrajeOuFalhar(itemDto.trajeId());
                traje.setCondicao(itemDto.condicao());
                traje.setStatus(StatusTraje.DISPONIVEL);
                trajeRepository.save(traje);
            });
        }

        DevolucaoResponse devolucaoResponse = devolucaoService.criar(dto, aluguel);

        BigDecimal multa = dto.valorMulta() != null ? dto.valorMulta() : BigDecimal.ZERO;
        aluguel.setValorMulta(multa);
        if (multa.compareTo(BigDecimal.ZERO) > 0) {
            aluguel.setValorTotal(aluguel.getValorTotal().add(multa));
        }

        aluguel.setStatus(StatusAluguel.CONCLUIDO);
        aluguelRepository.save(aluguel);

        return devolucaoResponse;
    }

    private void sincronizarStatusEmAtraso() {
        List<Aluguel> paraAtraso = aluguelRepository.findAlugueisAtrasados(StatusAluguel.ATIVO);
        paraAtraso.forEach(aluguel -> aluguel.setStatus(StatusAluguel.ATRASO));
        if (!paraAtraso.isEmpty()) {
            aluguelRepository.saveAll(paraAtraso);
        }

        List<Aluguel> paraReativar = aluguelRepository.findAlugueisComPrazoVigente(StatusAluguel.ATRASO);
        paraReativar.forEach(aluguel -> aluguel.setStatus(StatusAluguel.ATIVO));
        if (!paraReativar.isEmpty()) {
            aluguelRepository.saveAll(paraReativar);
        }
    }

    private void sincronizarStatusEmAtraso(Aluguel aluguel) {
        if (!STATUS_EM_ANDAMENTO.contains(aluguel.getStatus())) {
            return;
        }
        atualizarStatusConformePrazo(aluguel, aluguel.getDataDevolucao());
    }

    private void atualizarStatusConformePrazo(Aluguel aluguel, LocalDate dataDevolucaoPrevista) {
        LocalDate hoje = LocalDate.now();
        if (dataDevolucaoPrevista.isBefore(hoje)) {
            aluguel.setStatus(StatusAluguel.ATRASO);
            return;
        }
        if (aluguel.getStatus() == StatusAluguel.ATRASO) {
            aluguel.setStatus(StatusAluguel.ATIVO);
        }
    }


    // ===============================
    // HELPERS
    // ===============================
    private Aluguel buscarAluguelOuFalhar(Long id) {
        return aluguelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_ALUGUEL, id));
    }

    private Cliente buscarClienteOuFalhar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_CLIENTE, id));
    }

    private Traje buscarTrajeOuFalhar(Long id) {
        return trajeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_TRAJE, id));
    }

    private ItemAluguel criarItem(ItemAluguelRequest dto, Aluguel aluguel, Long aluguelIdParaIgnorar) {
        // Buscar traje
        Traje traje = buscarTrajeOuFalhar(dto.trajeId());

        // Validações
        validarTrajeDisponivel(traje);

        // Validação de conflito de período
        validarDisponibilidadePeriodo(
                dto.trajeId(),
                aluguel.getDataRetirada(),
                aluguel.getDataDevolucao(),
                aluguelIdParaIgnorar
        );

        return ItemAluguelMapper.toEntity(traje, aluguel);
    }

    private void validarDisponibilidadePeriodo(Long trajeId,
                                               LocalDate retirada,
                                               LocalDate devolucao,
                                               Long aluguelId) {
        boolean indisponivel = itemAluguelRepository
                .trajeIndisponivelNoPeriodo(trajeId, retirada, devolucao, aluguelId);

        if (indisponivel) {
            throw new BusinessException("Traje já está alugado nesse período");
        }
    }

    private void validarDatas(LocalDate retirada, LocalDate devolucao) {
        if (devolucao.isBefore(retirada)) {
            throw new BusinessException("A data de devolução deve ser após a data de retirada");
        }

        if (retirada.isBefore(LocalDate.now())) {
            throw new BusinessException("A data de retirada não pode ser no passado");
        }
    }

    private void validarTrajeDisponivel(Traje traje) {
        if (!traje.getStatus().equals(StatusTraje.DISPONIVEL)) {
            throw new BusinessException("Traje não está disponível");
        }
    }

    private List<ItemAluguel> criarItens(List<ItemAluguelRequest> itensDto, Aluguel aluguel, Long aluguelIdParaIgnorar) {
        return itensDto.stream()
                .map(itemDto -> criarItem(itemDto, aluguel, aluguelIdParaIgnorar))
                .toList();
    }

    private BigDecimal calcularValorTotal(List<ItemAluguel> itens) {
        return itens.stream()
                .map(item -> item.getTraje().getValorItem())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validarValorComDesconto(BigDecimal valorComDesconto) {
        if (valorComDesconto.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("O valor com desconto não pode ser negativo");
        }
    }
}
