package br.com.tourguide.api.TourGuideApiApplication.controller;

import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioRecord;
import br.com.tourguide.api.TourGuideApiApplication.infra.security.DadosTokenJWT;
import br.com.tourguide.api.TourGuideApiApplication.infra.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

// Teste retornando token para o front para ser salvo. Todo usar redis para salvar a senha
@RestController
@RequestMapping("api/auth/login")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity efetuarLogin(@RequestBody UsuarioRecord dados) {
        var token = new UsernamePasswordAuthenticationToken(dados.username(), dados.password());
        var authentication = manager.authenticate(token);

        var tokenJWT = tokenService.gerarToken((UsuarioRecord) authentication.getPrincipal());

        return ResponseEntity.ok(new DadosTokenJWT(tokenJWT));
    }
}
