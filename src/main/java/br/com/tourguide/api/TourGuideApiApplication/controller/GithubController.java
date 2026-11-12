package br.com.tourguide.api.TourGuideApiApplication.controller;

import br.com.tourguide.api.TourGuideApiApplication.domain.autenticacao.DadosLogin;
import br.com.tourguide.api.TourGuideApiApplication.domain.github.GithubService;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioEntity;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioRepository;
import br.com.tourguide.api.TourGuideApiApplication.infra.security.CookiesToken;
import br.com.tourguide.api.TourGuideApiApplication.infra.security.TokenService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("api/auth/login/github")
public class GithubController {

    private final GithubService githubService;
    private final TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CookiesToken cookiesToken;

    public GithubController(GithubService githubService, TokenService tokenService) {
        this.githubService = githubService;
        this.tokenService = tokenService;
    }

    @GetMapping
    public ResponseEntity<Void> redirecionarGithub() {
        var url = githubService.gerarUrl();

        var headers = new HttpHeaders();
        headers.setLocation(URI.create(url));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/autorizado")
    public ResponseEntity<DadosLogin> obterToken(@RequestParam String code) {

        String email = githubService.obterEmail(code);

        UsuarioEntity usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(EntityNotFoundException::new);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String tokenJWT = tokenService.gerarToken(usuario);
        String refreshTokenJWT = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok()
                .headers(cookiesToken.criar(tokenJWT, refreshTokenJWT))
                .body(new DadosLogin(false));
    }
}
