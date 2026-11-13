package br.com.tourguide.api.TourGuideApiApplication.controller;

import br.com.tourguide.api.TourGuideApiApplication.domain.autenticacao.DadosA2F;
import br.com.tourguide.api.TourGuideApiApplication.domain.autenticacao.DadosLogin;
import br.com.tourguide.api.TourGuideApiApplication.infra.security.CookiesToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioEntity;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioLoginRecord;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioRepository;
import br.com.tourguide.api.TourGuideApiApplication.infra.security.TokenService;
import br.com.tourguide.api.TourGuideApiApplication.infra.totp.TotpService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Todo usar redis para salvar a senha
@RestController
@RequestMapping("api/auth")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TotpService totpService;

    @Autowired
    private CookiesToken cookiesToken;

    /**
     * Gera o access token e o refresh token e os devolve em cookies HttpOnly (header {@code Set-Cookie}).
     * O corpo só informa {@code a2f = false}; assim os tokens nunca chegam ao JavaScript do front.
     */
    private ResponseEntity<DadosLogin> responderComTokens(UsuarioEntity usuario) {
        String tokenJWT = tokenService.gerarToken(usuario);
        String refreshTokenJWT = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok()
                .headers(cookiesToken.criar(tokenJWT, refreshTokenJWT))
                .body(new DadosLogin(false));
    }

    /**
     * Realiza o login do usuário e devolve um token JWT:
     * <ul>
     *   <li>Monta um {@code UsernamePasswordAuthenticationToken} com o username e a senha recebidos;</li>
     *   <li>O AuthenticationManager autentica esse token (busca o usuário no banco e compara a senha com BCrypt),
     *   lançando exceção se as credenciais forem inválidas;</li>
     *   <li>Com o usuário autenticado, ele valida se tem autenticação de dois fatores </li>
     *   <li>Caso tenha, ele retorna com o Boolean {@code a2f} como true, assim sendo validado em outro endpoint; </li>
     *   <li>Caso não tenha A2F ativo, o TokenService gera o token JWT e o refresh token JWT e os retorna como Cookie com status 200.</li>
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
    @PostMapping("/login")
    public ResponseEntity<DadosLogin> efetuarLogin(@RequestBody UsuarioLoginRecord dados) {
        UsernamePasswordAuthenticationToken tokenLogin = new UsernamePasswordAuthenticationToken(dados.username(), dados.password());
        Authentication authentication = manager.authenticate(tokenLogin);

        UsuarioEntity usuario = (UsuarioEntity) authentication.getPrincipal();
        if (usuario.isA2fAtiva()) {
            return ResponseEntity.ok(new DadosLogin(true));
        }

        return responderComTokens(usuario);
    }

    /**
     * Gera um novo par de tokens a partir de um refresh token válido, assim evitando ter pedir login e senha toda hora:
     * <ul>
     *   <li>Valida o refresh token (assinatura, validade e se é do tipo refresh), retornando 401 se for inválido;</li>
     *   <li>Busca o usuário pelo email que está no subject do refresh token;</li>
     *   <li>Retorna 200 com um novo token JWT e um novo refresh token indo pelo Cookies.</li>
     * </ul>
     * A ideia do refresh token além de recriar os tokens JWTs, é também ter uma segurança, pois caso vaze o access token,
     * ele expira rapidamento (30 minutos). Já o refresh token expira em 2 horas, o que é seguro pois fica menos exposto
     * que o access token.
     */
    @PostMapping("/login/atualizar-token")
    public ResponseEntity<DadosLogin> atualizarToken(HttpServletRequest request) {
        String refreshTokenJWT = cookiesToken.ler(request, CookiesToken.REFRESH_TOKEN);
        if (refreshTokenJWT == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String usuarioEmail = tokenService.getUsuarioFromRefreshToken(refreshTokenJWT);

        UsuarioEntity usuario = usuarioRepository.findByEmailIgnoreCase(usuarioEmail).orElseThrow(EntityNotFoundException::new);

        return responderComTokens(usuario);
    }

    /**
     * Segunda etapa do login para usuários com autenticação de dois fatores (A2F) ativa:
     * <ul>
     *   <li>É chamado depois que o {@code efetuarLogin} retorna {@code a2f} como true e os tokens JWT null;</li>
     *   <li>Recebe o email do usuário e o código de 6 dígitos gerado pelo app autenticador (Google Authenticator, Authy, etc.);</li>
     *   <li>Busca o usuário pelo email e o TotpService valida o código com o secret do A2F salvo no usuário;</li>
     *   <li>Se o código for inválido ou expirado, lança {@code BadCredentialsException}, retornando 401;</li>
     *   <li>Se o código for válido, o TokenService gera o token JWT e o refresh token JWT e os retorna nos Cookies com status 200.</li>
     * </ul>
     * O código do app autenticador (TOTP) muda a cada 30 segundos e é calculado a partir do secret compartilhado
     * na leitura do QR code ({@code configurar-a2f}), por isso só quem tem o celular cadastrado consegue gerá-lo.
     */
    @PostMapping("/login/verificar-a2f")
    public ResponseEntity<DadosLogin> verificarSegundoFator(@Valid @RequestBody DadosA2F dadosA2F) {
        var usuario = usuarioRepository.findByEmailIgnoreCase(dadosA2F.email()).orElseThrow();
        var codigoValido = totpService.verificarCodigo(dadosA2F.codigo(), usuario);
        if (!codigoValido) {
            throw new BadCredentialsException("Código inválido");
        }

        return responderComTokens(usuario);
    }

    /**
     * Logout: como o JS do front não enxerga os cookies HttpOnly, quem apaga é o servidor,
     * devolvendo os mesmos cookies vazios e com Max-Age 0.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().headers(cookiesToken.apagar()).build();
    }
 }
