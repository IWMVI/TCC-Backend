package br.edu.fateczl.tcc.repository;

import br.edu.fateczl.tcc.domain.Funcionario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByEmail(String email);

    Optional<Funcionario> findByTokenConfirmacao(String tokenConfirmacao);

    Optional<Funcionario> findByTokenRecuperacaoSenha(String tokenRecuperacaoSenha);

    boolean existsByEmail(String email);

    @Query("""
            SELECT f FROM Funcionario f
            WHERE LOWER(f.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
               OR LOWER(f.email) LIKE LOWER(CONCAT('%', :busca, '%'))
            """)
    Page<Funcionario> buscarPorTermoPaginado(@Param("busca") String busca, Pageable pageable);
}
