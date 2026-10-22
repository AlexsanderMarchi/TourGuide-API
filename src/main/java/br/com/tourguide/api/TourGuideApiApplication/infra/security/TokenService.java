package br.com.tourguide.api.TourGuideApiApplication.infra.security;

import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioRecord;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  // Secret vindo do application.propeties
  @Value("${api.security.token.secret}")
  private String secret;

  public String gerarToken(UsuarioRecord usuario) {
    try {
      var algoritmo = Algorithm.HMAC256(secret);
      return JWT.create()
          .withIssuer("API Voll.med")
          .withSubject(usuario.username())
          .withExpiresAt(dataExpiracao())
          .sign(algoritmo);

    } catch (JWTCreationException exception) {
      throw new RuntimeException("erro ao gerrar token jwt", exception);
    }
  }

  private Instant dataExpiracao() {
    return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
  }

}
