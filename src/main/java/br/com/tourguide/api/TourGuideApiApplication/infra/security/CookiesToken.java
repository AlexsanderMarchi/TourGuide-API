package br.com.tourguide.api.TourGuideApiApplication.infra.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Monta e lê os cookies que carregam os tokens JWT:
 * <ul>
 *   <li>{@code HttpOnly}: o JavaScript do front não consegue ler o cookie, então um XSS não rouba o token;</li>
 *   <li>{@code Secure}: o navegador só envia o cookie por HTTPS (localhost é exceção nos navegadores);</li>
 *   <li>{@code SameSite=Strict}: o navegador só envia o cookie em requisições que partem do próprio site,
 *   o que protege contra CSRF (por isso o CSRF do Spring pode continuar desabilitado);</li>
 *   <li>O refresh token tem {@code Path} restrito ao endpoint de atualizar token, assim ele não viaja
 *   em toda requisição, apenas quando é realmente necessário.</li>
 * </ul>
 */
@Component
public class CookiesToken {

    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";

    private static final String PATH_ACCESS = "/api/auth/login";
    private static final String PATH_REFRESH = "/api/auth/login/atualizar-token";

    // mesmos tempos de expiração do TokenService
    private static final Duration DURACAO_ACCESS = Duration.ofMinutes(30);
    private static final Duration DURACAO_REFRESH = Duration.ofHours(2);

    // permite desligar o Secure caso o front seja acessado por http fora do localhost
    @Value("${api.security.cookie.secure:true}")
    private boolean secure;

    /**
     * Headers {@code Set-Cookie} com os dois tokens, para serem colocados na resposta do login/refresh.
     */
    public HttpHeaders criar(String tokenJWT, String refreshTokenJWT) {
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, montar(ACCESS_TOKEN, tokenJWT, PATH_ACCESS, DURACAO_ACCESS).toString());
        headers.add(HttpHeaders.SET_COOKIE, montar(REFRESH_TOKEN, refreshTokenJWT, PATH_REFRESH, DURACAO_REFRESH).toString());
        return headers;
    }

    /**
     * Headers {@code Set-Cookie} que apagam os dois tokens (mesmo nome e path, com Max-Age 0), usados no logout.
     */
    public HttpHeaders apagar() {
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, montar(ACCESS_TOKEN, "", PATH_ACCESS, Duration.ZERO).toString());
        headers.add(HttpHeaders.SET_COOKIE, montar(REFRESH_TOKEN, "", PATH_REFRESH, Duration.ZERO).toString());
        return headers;
    }

    /**
     * Lê o valor de um cookie da requisição, ou null se não existir.
     */
    public String ler(HttpServletRequest request, String nome) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (nome.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie montar(String nome, String valor, String path, Duration duracao) {
        return ResponseCookie.from(nome, valor)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(path)
                .maxAge(duracao)
                .build();
    }
}
