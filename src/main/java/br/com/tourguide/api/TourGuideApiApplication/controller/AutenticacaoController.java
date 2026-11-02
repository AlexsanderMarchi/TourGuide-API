package br.com.tourguide.api.TourGuideApiApplication.controller;

import br.com.tourguide.api.TourGuideApiApplication.domain.autenticacao.DadosRefreshTokenJWT;
import br.com.tourguide.api.TourGuideApiApplication.domain.autenticacao.DadosTokenJWT;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioEntity;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioLoginRecord;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioRepository;
import br.com.tourguide.api.TourGuideApiApplication.infra.security.TokenService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Todo usar redis para salvar a senha
@RestController
@RequestMapping("api/auth/login")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Realiza o login do usuário e devolve um token JWT:
     * <ul>
     *   <li>Monta um {@code UsernamePasswordAuthenticationToken} com o username e a senha recebidos;</li>
     *   <li>O AuthenticationManager autentica esse token (busca o usuário no banco e compara a senha com BCrypt),
     *   lançando exceção se as credenciais forem inválidas;</li>
     *   <li>Com o usuário autenticado, o TokenService gera o token JWT e o refresh token JWT;</li>
     *   <li>Retorna 200 com os JWTs no corpo da resposta.</li>
     * </ul>
     * Diferença entre {@code tokenLogin}, {@code tokenJWT} e  {@code refreshTokenJWT}:
     * <ul>
     *   <li>{@code tokenLogin}: objeto interno do Spring Security que só carrega as credenciais (login e senha)
     *   para serem verificadas; existe apenas durante esta requisição e nunca sai do servidor;</li>
     *   <li>{@code tokenJWT}: String assinada gerada após a autenticação dar certo; é enviada ao front,
     *   que deve mandá-la nas próximas requisições para provar quem é, sem precisar reenviar a senha.</li>
     *   <li>{@code refreshTokenJWT}: usado para o endpoint {@code atualizarToken} recriar o access token, assim como o proprio refresh.
     *   Útil para nao ter que pedir login novamente ao cliente ao expirar o token</li>
     * </ul>
     */
    @PostMapping
    public ResponseEntity efetuarLogin(@RequestBody UsuarioLoginRecord dados) {
        UsernamePasswordAuthenticationToken tokenLogin = new UsernamePasswordAuthenticationToken(dados.username(), dados.password());
        Authentication authentication = manager.authenticate(tokenLogin);

        String tokenJWT = tokenService.gerarToken((UsuarioEntity) authentication.getPrincipal());
        String refreshTokenJWT = tokenService.gerarRefreshToken((UsuarioEntity) authentication.getPrincipal());

        return ResponseEntity.ok(new DadosTokenJWT(tokenJWT, refreshTokenJWT));
    }

    /**
     * Gera um novo par de tokens a partir de um refresh token válido, assim evitando ter pedir login e senha:
     * <ul>
     *   <li>Valida o refresh token (assinatura, validade e se é do tipo refresh), retornando 401 se for inválido;</li>
     *   <li>Busca o usuário pelo email que está no subject do refresh token;</li>
     *   <li>Retorna 200 com um novo token JWT e um novo refresh token.</li>
     * </ul>
     * A ideia do refresh token além de recriar os tokens JWTs, é também ter uma segurança, pois caso vaze o access token,
     * ele expira rapidamento (30 minutos). Já o refresh token expira em 2 horas, o que é seguro pois fica menos exposto
     * que o access token.
     */
    @PostMapping("/atualizar-token")
    public ResponseEntity atualizarToken(@RequestBody DadosRefreshTokenJWT dados) {
        String usuarioEmail = tokenService.getUsuarioFromRefreshToken(dados.refreshTokenJWT());

        UsuarioEntity usuario = usuarioRepository.findByEmailIgnoreCase(usuarioEmail)
                .orElseThrow(EntityNotFoundException::new);

        String tokenJWT = tokenService.gerarToken(usuario);
        String refreshTokenJWT = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new DadosTokenJWT(tokenJWT, refreshTokenJWT));
    }

    //todo Testar email em recuperar senha. Terminar testes e refresh token. Validar comentarios e validação de erros
}
