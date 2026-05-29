package br.edu.fateczl.tcc.service;

import br.edu.fateczl.tcc.domain.Funcionario;
import br.edu.fateczl.tcc.dto.funcionario.FuncionarioRequest;
import br.edu.fateczl.tcc.dto.funcionario.FuncionarioResponse;
import br.edu.fateczl.tcc.dto.funcionario.FuncionarioUpdateRequest;
import br.edu.fateczl.tcc.exception.BusinessException;
import br.edu.fateczl.tcc.exception.ResourceNotFoundException;
import br.edu.fateczl.tcc.mapper.FuncionarioMapper;
import br.edu.fateczl.tcc.repository.FuncionarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class FuncionarioService {

    private static final String RESOURCE = "Funcionário";

    private final FuncionarioRepository repository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public FuncionarioService(
            FuncionarioRepository repository,
            AuthService authService,
            PasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public FuncionarioResponse criar(FuncionarioRequest request) {
        return authService.criarFuncionario(request.nome(), request.email(), request.senha());
    }

    public Page<FuncionarioResponse> listar(String busca, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho);
        if (busca == null || busca.isBlank()) {
            return repository.findAll(pageable).map(FuncionarioMapper::toResponse);
        }
        return repository.buscarPorTermoPaginado(busca.trim(), pageable)
                .map(FuncionarioMapper::toResponse);
    }

    public FuncionarioResponse buscarPorId(Long id) {
        return FuncionarioMapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public FuncionarioResponse atualizar(Long id, FuncionarioUpdateRequest request) {
        Funcionario funcionario = buscarEntidade(id);
        String emailNormalizado = request.email().trim().toLowerCase();

        repository.findByEmail(emailNormalizado)
                .filter(f -> !f.getId().equals(id))
                .ifPresent(f -> {
                    throw new BusinessException("E-mail já cadastrado");
                });

        funcionario.setNome(request.nome().trim());
        funcionario.setEmail(emailNormalizado);

        if (request.senha() != null && !request.senha().isBlank()) {
            funcionario.setSenha(passwordEncoder.encode(request.senha()));
        }

        repository.save(funcionario);
        return FuncionarioMapper.toResponse(funcionario);
    }

    @Transactional
    public void desativar(Long id) {
        Funcionario funcionario = buscarEntidade(id);
        funcionario.setAtivo(false);
        repository.save(funcionario);
    }

    private Funcionario buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE, id));
    }
}
