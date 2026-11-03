package br.com.tourguide.api.TourGuideApiApplication.domain.github;

import br.com.tourguide.api.TourGuideApiApplication.domain.senha.DadosAlteracaoSenha;
import br.com.tourguide.api.TourGuideApiApplication.domain.senha.DadosEsqueciSenha;
import br.com.tourguide.api.TourGuideApiApplication.domain.senha.DadosRecuperacaoSenha;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioEntity;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioRepository;
import br.com.tourguide.api.TourGuideApiApplication.infra.email.EmailService;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 *
 * <ul>
 *     <li></li>
 *     <li></li>
 * </ul>
 *
 */
@Service
public class GithubService {

    private final String clientId = "teste";
//    private final String clientSecret = "teste";
    private final String redirectUri = "http://localhost:5173";
//    private final RestClient restClient;

//    public GithubService(RestClient.Builder restClientBuilder) {
//        this.restClient = restClientBuilder.build();
//    }

    public String gerarUrl(){
        return "https://github.com/login/oauth/authorize"+
                "?client_id="+clientId +
                "&redirect_uri="+redirectUri +
                "&scope=read:user,user:email";
    }

//    public String obterToken(String code) {
//        var resposta = restClient.post()
//                .uri("https://github.com/login/oauth/access_token")
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .body(Map.of("code", code, "client_id", clientId,
//                        "client_secret", clientSecret, "redirect_uri", redirectUri))
//                .retrieve()
//                .body(String.class);
//        return resposta;
//    }
}
