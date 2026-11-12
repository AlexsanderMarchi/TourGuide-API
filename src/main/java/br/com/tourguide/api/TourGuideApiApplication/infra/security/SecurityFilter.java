package br.com.tourguide.api.TourGuideApiApplication.infra.security;

import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que autentica o usuário a cada requisição a partir do token JWT:
 * <ul>
 *   <li>Estende {@code OncePerRequestFilter}, garantindo que roda uma única vez por requisição;</li>
 *   <li>É registrado no ConfigurationSecurity antes do {@code UsernamePasswordAuthenticationFilter}, para que o usuário
 *   já esteja autenticado quando o Spring Security checar as regras de acesso;</li>
 * </ul>
 */
@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private CookiesToken cookiesToken;

    /**
     * Executado a cada requisição:
     * <ul>
     *   <li>Recupera o token JWT do cookie {@code access_token} (ou do header {@code Authorization} para testes);</li>
     *   <li>Se houver token, o TokenService valida (assinatura, validade e se é do tipo access) e extrai o email do subject;</li>
     *   <li>Busca o usuário pelo email e, se encontrar, coloca-o no {@code SecurityContextHolder} junto com os perfis
     *   (authorities), que são usados pelo {@code @PreAuthorize} e pelo {@code @AuthenticationPrincipal};</li>
     *   <li>Se não houver token, segue sem autenticar: rotas públicas funcionam normalmente e as protegidas retornam 401;</li>
     *   <li>Por fim, chama {@code filterChain.doFilter} para a requisição seguir para os próximos filtros e o controller.</li>
     * </ul>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tokenJWT = recuperarToken(request);

        if (tokenJWT != null) {
            var usuarioEmail = tokenService.getUsuarioFromTokenJWT(tokenJWT);

            repository.findByEmailIgnoreCase(usuarioEmail).ifPresent(usuario -> {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }

    /**
     * O navegador manda o token no cookie HttpOnly {@code access_token}.
     * a validação do heade é usado para testes via Postman/Insomnia.
     */
    private String recuperarToken(HttpServletRequest request) {
        String tokenCookie = cookiesToken.ler(request, CookiesToken.ACCESS_TOKEN);
        if (tokenCookie != null) {
            return tokenCookie;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            return authorizationHeader.replace("Bearer ", "");
        }

        return null;
    }
}
