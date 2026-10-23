package br.com.tourguide.api.TourGuideApiApplication.infra.security;

import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioEntity;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Responsável por gerar os tokens JWT da API:
 * <ul>
 *   <li>Assina o token com HMAC256 usando o secret do application.properties;</li>
 *   <li>Coloca o username do usuário como subject;</li>
 *   <li>Define validade de 2 horas.</li>
 * </ul>
 */
@Service
public class TokenService {

  // Secret vindo do application.propeties
  @Value("${api.security.token.secret}")
  private String secret;

  public String gerarToken(UsuarioEntity usuario) {
    try {
      var algoritmo = Algorithm.HMAC256(secret);
      return JWT.create()
          .withIssuer("API Tour.Guide")
          .withSubject(usuario.getUsername())
          .withExpiresAt(dataExpiracao())
          .sign(algoritmo);

    } catch (JWTCreationException exception) {
      throw new RuntimeException("erro ao gerrar token jwt", exception);
    }
  }

  public String getUsuarioFromTokenJWT(String tokenJWT) {
    try {
      var algoritmo = Algorithm.HMAC256(secret);
      return JWT.require(algoritmo)
              .withIssuer("API Tour.Guide")
              .build()
              .verify(tokenJWT)
              .getSubject();
    } catch (JWTVerificationException exception) {
      throw new RuntimeException("Token JWT inválido ou expirado!");
    }
  }

  private Instant dataExpiracao() {
    return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
  }

}
