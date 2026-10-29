package br.com.tourguide.api.TourGuideApiApplication.controller;

import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioEntity;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioRecord;
import br.com.tourguide.api.TourGuideApiApplication.infra.security.DadosTokenJWT;
import br.com.tourguide.api.TourGuideApiApplication.infra.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

// Teste retornando token para o front para ser salvo.
// Todo usar redis para salvar a senha
@RestController
@RequestMapping("api/auth/login")
@RequiredArgsConstructor
public class AutenticacaoController {

    private AuthenticationManager manager;
    private TokenService tokenService;

    /** Realiza o login do usuário e devolve um token JWT:
     * <ul>
     *   <li>Monta um {@code UsernamePasswordAuthenticationToken} com o username e a senha recebidos;</li>
     *   <li>O AuthenticationManager autentica esse token (busca o usuário no banco e compara a senha com BCrypt),
     *   lançando exceção se as credenciais forem inválidas;</li>
     *   <li>Com o usuário autenticado, o TokenService gera o token JWT;</li>
     *   <li>Retorna 200 com o JWT no corpo da resposta.</li>
     * </ul>
     * Diferença entre {@code token} e {@code tokenJWT}:
     * <ul>
     *   <li>{@code token}: objeto interno do Spring Security que só carrega as credenciais (login e senha)
     *   para serem verificadas; existe apenas durante esta requisição e nunca sai do servidor;</li>
     *   <li>{@code tokenJWT}: String assinada gerada após a autenticação dar certo; é enviada ao front,
     *   que deve mandá-la nas próximas requisições para provar quem é, sem precisar reenviar a senha.</li>
     * </ul>
     */
    @PostMapping
    public ResponseEntity efetuarLogin(@RequestBody UsuarioRecord dados) {
        var token = new UsernamePasswordAuthenticationToken(dados.username(), dados.password());
        var authentication = manager.authenticate(token);

        String tokenJWT = tokenService.gerarToken((UsuarioEntity) authentication.getPrincipal());

        return ResponseEntity.ok(new DadosTokenJWT(tokenJWT));
    }

    //todo melhorar pr template add tratamento de erro para 403 e outros. Testar email em recuperar senha, melhorar swagger. Criar testes. remover autowired como foi feito aqui
}
