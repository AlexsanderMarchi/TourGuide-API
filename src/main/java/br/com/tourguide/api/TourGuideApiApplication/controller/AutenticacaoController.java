package br.com.tourguide.api.TourGuideApiApplication.controller;

import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioRecord;
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

    @PostMapping
    public ResponseEntity efetuarLogin(@RequestBody UsuarioRecord dados) {
        var token = new UsernamePasswordAuthenticationToken(dados.username(), dados.password());
        var authenticaon = manager.authenticate(token);

        return ResponseEntity.ok().build();
    }
}
