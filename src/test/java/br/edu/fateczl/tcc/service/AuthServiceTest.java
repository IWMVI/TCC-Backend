package br.edu.fateczl.tcc.service;

import br.edu.fateczl.tcc.config.AppAuthProperties;
import br.edu.fateczl.tcc.config.AppProperties;
import br.edu.fateczl.tcc.domain.Funcionario;
import br.edu.fateczl.tcc.dto.auth.LoginRequest;
import br.edu.fateczl.tcc.exception.BusinessException;
import br.edu.fateczl.tcc.repository.FuncionarioRepository;
import br.edu.fateczl.tcc.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppAuthProperties authProperties;

    @InjectMocks
    private AuthService authService;

    private Funcionario funcionario;

    @BeforeEach
    void setUp() {
        funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setNome("Teste");
        funcionario.setEmail("teste@locadora.local");
        funcionario.setSenha("hash");
        funcionario.setAtivo(true);
        funcionario.setEmailVerificado(true);
    }

    @Test
    void login_deveRetornarTokenQuandoCredenciaisValidas() {
        when(funcionarioRepository.findByEmail("teste@locadora.local")).thenReturn(Optional.of(funcionario));
        when(passwordEncoder.matches("senha123", "hash")).thenReturn(true);
        when(jwtService.gerarToken(funcionario)).thenReturn("token-jwt");

        var resposta = authService.login(new LoginRequest("teste@locadora.local", "senha123"));

        assertEquals("token-jwt", resposta.token());
        assertEquals("Teste", resposta.funcionario().nome());
    }

    @Test
    void login_deveFalharQuandoEmailNaoVerificado() {
        funcionario.setEmailVerificado(false);
        when(funcionarioRepository.findByEmail("teste@locadora.local")).thenReturn(Optional.of(funcionario));
        when(passwordEncoder.matches("senha123", "hash")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                authService.login(new LoginRequest("teste@locadora.local", "senha123")));

        assertEquals("E-mail não verificado. Confirme seu e-mail antes de entrar.", ex.getMessage());
    }

    @Test
    void confirmarEmail_deveAtivarConta() {
        funcionario.setEmailVerificado(false);
        funcionario.setTokenConfirmacao("token-abc");
        when(funcionarioRepository.findByTokenConfirmacao("token-abc")).thenReturn(Optional.of(funcionario));
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(inv -> inv.getArgument(0));

        var resposta = authService.confirmarEmail("token-abc");

        assertEquals(true, resposta.emailVerificado());
        verify(funcionarioRepository).save(funcionario);
    }
}
