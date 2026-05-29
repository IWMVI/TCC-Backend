package br.edu.fateczl.tcc.service;

import br.edu.fateczl.tcc.config.AppAuthProperties;
import br.edu.fateczl.tcc.config.AppProperties;
import br.edu.fateczl.tcc.domain.Funcionario;
import br.edu.fateczl.tcc.dto.auth.LoginRequest;
import br.edu.fateczl.tcc.dto.auth.LoginResponse;
import br.edu.fateczl.tcc.dto.auth.RegistrarFuncionarioRequest;
import br.edu.fateczl.tcc.dto.funcionario.FuncionarioResponse;
import br.edu.fateczl.tcc.exception.BusinessException;
import br.edu.fateczl.tcc.mapper.FuncionarioMapper;
import br.edu.fateczl.tcc.repository.FuncionarioRepository;
import br.edu.fateczl.tcc.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final int HORAS_EXPIRACAO_TOKEN = 24;
    private static final int HORAS_EXPIRACAO_RECUPERACAO_SENHA = 2;

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AppProperties appProperties;
    private final AppAuthProperties authProperties;

    public AuthService(
            FuncionarioRepository funcionarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService,
            AppProperties appProperties,
            AppAuthProperties authProperties
    ) {
        this.funcionarioRepository = funcionarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.appProperties = appProperties;
        this.authProperties = authProperties;
    }

    public LoginResponse login(LoginRequest request) {
        Funcionario funcionario = funcionarioRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos"));

        if (!funcionario.isAtivo()) {
            throw new BusinessException("Conta desativada. Entre em contato com o administrador.");
        }

        if (!passwordEncoder.matches(request.senha(), funcionario.getSenha())) {
            throw new BusinessException("E-mail ou senha inválidos");
        }

        if (!funcionario.isEmailVerificado()) {
            throw new BusinessException("E-mail não verificado. Confirme seu e-mail antes de entrar.");
        }

        String token = jwtService.gerarToken(funcionario);
        return new LoginResponse(token, FuncionarioMapper.toResponse(funcionario));
    }

    @Transactional
    public FuncionarioResponse registrar(RegistrarFuncionarioRequest request) {
        if (!authProperties.isRegistroPublico()) {
            throw new BusinessException("Registro público desabilitado");
        }
        return criarFuncionario(request.nome(), request.email(), request.senha());
    }

    @Transactional
    public FuncionarioResponse confirmarEmail(String token) {
        Funcionario funcionario = funcionarioRepository.findByTokenConfirmacao(token)
                .orElseThrow(() -> new BusinessException("Token de confirmação inválido"));

        if (funcionario.getTokenExpiraEm() != null && funcionario.getTokenExpiraEm().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token de confirmação expirado. Solicite um novo e-mail.");
        }

        funcionario.setEmailVerificado(true);
        funcionario.setTokenConfirmacao(null);
        funcionario.setTokenExpiraEm(null);
        funcionarioRepository.save(funcionario);

        return FuncionarioMapper.toResponse(funcionario);
    }

    @Transactional
    public void reenviarConfirmacao(String email) {
        Funcionario funcionario = funcionarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("Funcionário não encontrado"));

        if (funcionario.isEmailVerificado()) {
            throw new BusinessException("E-mail já verificado");
        }

        gerarTokenEEnviarEmail(funcionario);
        funcionarioRepository.save(funcionario);
    }

    @Transactional
    public void solicitarRecuperacaoSenha(String email) {
        funcionarioRepository.findByEmail(email.trim().toLowerCase())
                .filter(Funcionario::isAtivo)
                .ifPresent(funcionario -> {
                    String token = UUID.randomUUID().toString();
                    funcionario.setTokenRecuperacaoSenha(token);
                    funcionario.setTokenRecuperacaoExpiraEm(
                            LocalDateTime.now().plusHours(HORAS_EXPIRACAO_RECUPERACAO_SENHA)
                    );
                    String link = appProperties.getFrontendUrl() + "/redefinir-senha?token=" + token;
                    emailService.enviarRecuperacaoSenha(funcionario.getEmail(), funcionario.getNome(), link);
                    funcionarioRepository.save(funcionario);
                });
    }

    @Transactional
    public void redefinirSenha(String token, String senha) {
        Funcionario funcionario = funcionarioRepository.findByTokenRecuperacaoSenha(token)
                .orElseThrow(() -> new BusinessException("Link de recuperação inválido"));

        if (funcionario.getTokenRecuperacaoExpiraEm() != null
                && funcionario.getTokenRecuperacaoExpiraEm().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Link de recuperação expirado. Solicite um novo e-mail.");
        }

        funcionario.setSenha(passwordEncoder.encode(senha));
        funcionario.setTokenRecuperacaoSenha(null);
        funcionario.setTokenRecuperacaoExpiraEm(null);
        funcionarioRepository.save(funcionario);
    }

    @Transactional
    public FuncionarioResponse criarFuncionario(String nome, String email, String senha) {
        String emailNormalizado = email.trim().toLowerCase();

        if (funcionarioRepository.existsByEmail(emailNormalizado)) {
            throw new BusinessException("E-mail já cadastrado");
        }

        Funcionario funcionario = new Funcionario();
        funcionario.setNome(nome.trim());
        funcionario.setEmail(emailNormalizado);
        funcionario.setSenha(passwordEncoder.encode(senha));
        funcionario.setEmailVerificado(false);
        funcionario.setAtivo(true);

        gerarTokenEEnviarEmail(funcionario);
        funcionarioRepository.save(funcionario);

        return FuncionarioMapper.toResponse(funcionario);
    }

    private void gerarTokenEEnviarEmail(Funcionario funcionario) {
        String token = UUID.randomUUID().toString();
        funcionario.setTokenConfirmacao(token);
        funcionario.setTokenExpiraEm(LocalDateTime.now().plusHours(HORAS_EXPIRACAO_TOKEN));

        String link = appProperties.getFrontendUrl() + "/confirmar-email?token=" + token;
        emailService.enviarConfirmacaoEmail(funcionario.getEmail(), funcionario.getNome(), link);
    }
}
