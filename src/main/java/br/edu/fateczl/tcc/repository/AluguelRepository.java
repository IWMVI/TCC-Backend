package br.edu.fateczl.tcc.repository;

import br.edu.fateczl.tcc.domain.Aluguel;
import br.edu.fateczl.tcc.enums.StatusAluguel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AluguelRepository extends JpaRepository<Aluguel, Long>,
                                           JpaSpecificationExecutor<Aluguel> {

    long countByStatus(StatusAluguel status);

    @Query("SELECT COALESCE(SUM(a.valorTotal), 0) FROM aluguel a WHERE a.status = :status " +
           "AND a.dataAluguel BETWEEN :inicio AND :fim")
    BigDecimal somarValorTotalPorStatusEPeriodo(
            @Param("status") StatusAluguel status,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(a.valorTotal), 0) FROM aluguel a WHERE a.status IN :statuses")
    BigDecimal somarValorTotalPorStatuses(@Param("statuses") List<StatusAluguel> statuses);

    @Query("SELECT COALESCE(SUM(a.valorDesconto), 0) FROM aluguel a")
    BigDecimal somarTotalDescontos();

    @Query("SELECT COALESCE(SUM(a.valorMulta), 0) FROM aluguel a")
    BigDecimal somarTotalMultas();

    @Query("SELECT COALESCE(SUM(a.valorDesconto), 0) FROM aluguel a WHERE a.dataAluguel BETWEEN :inicio AND :fim")
    BigDecimal somarDescontosPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(a.valorMulta), 0) FROM aluguel a WHERE a.dataAluguel BETWEEN :inicio AND :fim")
    BigDecimal somarMultasPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(a.valorTotal), 0) FROM aluguel a WHERE a.dataAluguel BETWEEN :inicio AND :fim")
    BigDecimal somarReceitaBrutaPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    long countByDataAluguelBetween(LocalDate inicio, LocalDate fim);

    @Query("SELECT a.status, COUNT(a), COALESCE(SUM(a.valorTotal), 0) FROM aluguel a " +
           "WHERE a.dataAluguel BETWEEN :inicio AND :fim GROUP BY a.status")
    List<Object[]> agregarPorStatusEPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @EntityGraph(attributePaths = {"cliente"})
    List<Aluguel> findTop5ByOrderByDataAluguelDesc();

    List<Aluguel> findByClienteId(Long clienteId);

    List<Aluguel> findByStatus(StatusAluguel status);

    @Query("SELECT a FROM aluguel a WHERE a.cliente.id = :clienteId AND a.status = :status")
    List<Aluguel> findByClienteIdAndStatus(
            @Param("clienteId") Long clienteId,
            @Param("status") StatusAluguel status);

    @Query("SELECT a FROM aluguel a WHERE a.dataDevolucao < CURRENT_DATE AND a.status = :status")
    List<Aluguel> findAlugueisAtrasados(@Param("status") StatusAluguel status);

    @Query("SELECT a FROM aluguel a WHERE a.dataDevolucao >= CURRENT_DATE AND a.status = :status")
    List<Aluguel> findAlugueisComPrazoVigente(@Param("status") StatusAluguel status);

    @Query("SELECT a FROM aluguel a JOIN FETCH a.cliente WHERE a.id = :id")
    Optional<Aluguel> findByIdWithCliente(@Param("id") Long id);

    @EntityGraph(attributePaths = {"cliente", "itens", "itens.traje"})
    Optional<Aluguel> findWithRelacionamentosById(Long id);
}
