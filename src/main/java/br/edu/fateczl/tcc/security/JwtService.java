package br.edu.fateczl.tcc.security;

import br.edu.fateczl.tcc.config.AppAuthProperties;
import br.edu.fateczl.tcc.domain.Funcionario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final AppAuthProperties authProperties;

    public JwtService(AppAuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public String gerarToken(Funcionario funcionario) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + authProperties.getJwtExpirationMs());

        return Jwts.builder()
                .subject(funcionario.getEmail())
                .claim("funcionarioId", funcionario.getId())
                .claim("nome", funcionario.getNome())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(chaveSecreta())
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public Long extrairFuncionarioId(String token) {
        Object valor = extrairClaims(token).get("funcionarioId");
        if (valor instanceof Integer inteiro) {
            return inteiro.longValue();
        }
        if (valor instanceof Long longo) {
            return longo;
        }
        return null;
    }

    public boolean tokenValido(String token) {
        try {
            Claims claims = extrairClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chaveSecreta())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey chaveSecreta() {
        byte[] bytes = authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, Math.min(bytes.length, 32));
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
