package br.edu.fateczl.tcc.controller;

import br.edu.fateczl.tcc.dto.auth.LoginRequest;
import br.edu.fateczl.tcc.dto.auth.LoginResponse;
import br.edu.fateczl.tcc.dto.auth.RedefinirSenhaRequest;
import br.edu.fateczl.tcc.dto.auth.ReenviarConfirmacaoRequest;
import br.edu.fateczl.tcc.dto.auth.RegistrarFuncionarioRequest;
import br.edu.fateczl.tcc.dto.auth.SolicitarRecuperacaoSenhaRequest;
import br.edu.fateczl.tcc.dto.funcionario.FuncionarioResponse;
import br.edu.fateczl.tcc.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth Controller", description = "Autenticação e confirmação de e-mail")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login com e-mail e senha")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Registrar funcionário (se registro público habilitado)")
    @PostMapping("/registrar")
    public ResponseEntity<FuncionarioResponse> registrar(@Valid @RequestBody RegistrarFuncionarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @Operation(summary = "Confirmar e-mail via token")
    @GetMapping("/confirmar-email")
    public ResponseEntity<FuncionarioResponse> confirmarEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.confirmarEmail(token));
    }

    @Operation(summary = "Reenviar e-mail de confirmação")
    @PostMapping("/reenviar-confirmacao")
    public ResponseEntity<Void> reenviarConfirmacao(@Valid @RequestBody ReenviarConfirmacaoRequest request) {
        authService.reenviarConfirmacao(request.email());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Solicitar recuperação de senha por e-mail")
    @PostMapping("/recuperar-senha")
    public ResponseEntity<Void> solicitarRecuperacaoSenha(
            @Valid @RequestBody SolicitarRecuperacaoSenhaRequest request
    ) {
        authService.solicitarRecuperacaoSenha(request.email());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Redefinir senha via token do e-mail")
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        authService.redefinirSenha(request.token(), request.senha());
        return ResponseEntity.noContent().build();
    }
}
