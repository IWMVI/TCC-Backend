package br.edu.fateczl.tcc.security;

import br.edu.fateczl.tcc.repository.FuncionarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class FuncionarioUserDetailsService implements UserDetailsService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioUserDetailsService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return funcionarioRepository.findByEmail(username)
                .map(FuncionarioUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Funcionário não encontrado"));
    }
}
