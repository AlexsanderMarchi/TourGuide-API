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
          .withClaim("tipo", "access")
          .withExpiresAt(dataExpiracao())
          .sign(algoritmo);

    } catch (JWTCreationException exception) {
      throw new RuntimeException("erro ao gerar token jwt", exception);
    }
  }

  // tem um problema que nao é pego o tratamento de erro pelo SecurityFilter. Todo ver oq fazer
  public String gerarRefreshToken(UsuarioEntity usuario) {
    try {
      var algoritmo = Algorithm.HMAC256(secret);
      return JWT.create()
              .withIssuer("API Tour.Guide")
              .withSubject(usuario.getEmail())
              .withClaim("tipo", "refresh")
              .withExpiresAt(dataExpiracaoRefresh())
              .sign(algoritmo);

    } catch (JWTCreationException exception) {
      throw new RuntimeException("erro ao gerar refresh token jwt", exception);
    }
  }


  public String getUsuarioFromTokenJWT(String tokenJWT) {
    try {
      var algoritmo = Algorithm.HMAC256(secret);
      return JWT.require(algoritmo)
              .withIssuer("API Tour.Guide")
              .withClaim("tipo", "access")
              .build()
              .verify(tokenJWT)
              .getSubject();
    } catch (JWTVerificationException exception) {
      throw new RuntimeException("Token JWT inválido ou expirado!");
    }
  }

  // Lança JWTVerificationException se o refresh token for inválido/expirado (tratada como 401 no ErrorsValidation)
  public String getUsuarioFromRefreshToken(String refreshTokenJWT) {
    var algoritmo = Algorithm.HMAC256(secret);
    return JWT.require(algoritmo)
            .withIssuer("API Tour.Guide")
            .withClaim("tipo", "refresh")
            .build()
            .verify(refreshTokenJWT)
            .getSubject();
  }

  private Instant dataExpiracao() {
    return LocalDateTime.now().plusMinutes(30).toInstant(ZoneOffset.of("-03:00"));
  }

  private Instant dataExpiracaoRefresh() {
    return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
  }

}
