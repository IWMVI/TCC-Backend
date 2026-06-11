package br.edu.fateczl.tcc.repository;

import br.edu.fateczl.tcc.domain.Aluguel;
import br.edu.fateczl.tcc.enums.StatusAluguel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AluguelRepository extends JpaRepository<Aluguel, Long>,
                                           JpaSpecificationExecutor<Aluguel> {

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
