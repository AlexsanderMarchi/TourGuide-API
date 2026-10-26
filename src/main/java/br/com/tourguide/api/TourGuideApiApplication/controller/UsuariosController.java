package br.com.tourguide.api.TourGuideApiApplication.controller;

import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.DadosAlteracaoSenha;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioEntity;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioRepository;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioResponseDTO;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para o módulo de Usuarios, com listagem, criação, atualização e exclusão
 * Obs: Não é usado para a autenticação. Isso é feito pelo UserDetailsService no arquivo UsuarioService.java
 * */
@RestController
@RequestMapping("api/usuario")
@AllArgsConstructor
public class UsuariosController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @GetMapping("/lista")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> buscarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll().stream() //findAll ja é mét_odo padrao do JPA.
                .map(UsuarioResponseDTO::new)
                .toList());
    }

    @PostMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenha(@RequestBody @Valid DadosAlteracaoSenha dados, @AuthenticationPrincipal UsuarioEntity logado){
        usuarioService.alterarSenha(dados, logado);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("esqueci-minha-senha")
//    public String enviarTokenEmail(@ModelAttribute("email") String email, Model model){
//        try {
//            usuarioService.enviarToken(email);
//            return "redirect:esqueci-minha-senha?verificar";
//        } catch (RegraDeNegocioException e){
//            model.addAttribute("erro", e.getMessage());
//            return FORMULARIO_RECUPERACAO_SENHA;
//        }
//    }
}
