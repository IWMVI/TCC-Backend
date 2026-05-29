package br.edu.fateczl.tcc.seeder;

import br.edu.fateczl.tcc.config.AppAuthProperties;
import br.edu.fateczl.tcc.domain.Funcionario;
import br.edu.fateczl.tcc.repository.FuncionarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class FuncionarioInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FuncionarioInitializer.class);

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppAuthProperties authProperties;

    public FuncionarioInitializer(
            FuncionarioRepository funcionarioRepository,
            PasswordEncoder passwordEncoder,
            AppAuthProperties authProperties
    ) {
        this.funcionarioRepository = funcionarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
    }

    @Override
    public void run(String... args) {
        String adminEmail = authProperties.getAdminEmail().trim().toLowerCase();
        if (funcionarioRepository.existsByEmail(adminEmail)) {
            return;
        }

        Funcionario admin = new Funcionario();
        admin.setNome(authProperties.getAdminNome());
        admin.setEmail(adminEmail);
        admin.setSenha(passwordEncoder.encode(authProperties.getAdminSenha()));
        admin.setEmailVerificado(true);
        admin.setAtivo(true);
        funcionarioRepository.save(admin);

        log.info("Funcionário administrador criado: {} (e-mail já verificado)", adminEmail);
    }
}
