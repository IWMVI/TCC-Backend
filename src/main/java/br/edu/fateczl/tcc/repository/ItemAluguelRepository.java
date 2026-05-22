package br.edu.fateczl.tcc.repository;

import br.edu.fateczl.tcc.domain.ItemAluguel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ItemAluguelRepository extends JpaRepository<ItemAluguel, Long> {

    List<ItemAluguel> findByAluguelId(Long aluguelId);

    List<ItemAluguel> findByTrajeId(Long trajeId);

    @Query("""
        SELECT ia FROM item_aluguel ia
        WHERE ia.traje.id = :trajeId
          AND ia.aluguel.status IN ('ATIVO', 'ATRASO')
        """)
    java.util.Optional<ItemAluguel> findAtivoByTrajeId(@Param("trajeId") Long trajeId);

    @Query("SELECT ia FROM item_aluguel ia JOIN FETCH ia.traje WHERE ia.aluguel.id = :aluguelId")
    List<ItemAluguel> findByAluguelIdWithTraje(@Param("aluguelId") Long aluguelId);

    /**
     * Verifica se um traje está indisponível em um determinado período.
     * Regra:
     * - Quando aluguelId é NULL: usado na criação, verifica qualquer conflito.
     * - Quando aluguelId NÃO é NULL: usado na atualização,
     *   desconsidera o próprio aluguel para não gerar falso conflito.
     */
    @Query("""
        SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
        FROM item_aluguel i
        WHERE i.traje.id = :trajeId
          AND i.aluguel.status IN ('ATIVO', 'ATRASO')
          AND (:aluguelId IS NULL OR i.aluguel.id <> :aluguelId)
          AND (
               :dataRetirada <= i.aluguel.dataDevolucao
           AND :dataDevolucao >= i.aluguel.dataRetirada
          )
    """)
    boolean trajeIndisponivelNoPeriodo(
            @Param("trajeId") Long trajeId,
            @Param("dataRetirada") LocalDate dataRetirada,
            @Param("dataDevolucao") LocalDate dataDevolucao,
            @Param("aluguelId") Long aluguelId
    );

    /**
     * Retorna todos os períodos (dataRetirada, dataDevolucao) de aluguéis
     * ATIVOS que contêm o traje informado.
     */
    @Query("""
        SELECT i.aluguel.dataRetirada, i.aluguel.dataDevolucao
        FROM item_aluguel i
        WHERE i.traje.id = :trajeId
          AND i.aluguel.status IN ('ATIVO', 'ATRASO')
        ORDER BY i.aluguel.dataRetirada
    """)
    List<Object[]> findPeriodosAlugadosByTrajeId(@Param("trajeId") Long trajeId);
}
