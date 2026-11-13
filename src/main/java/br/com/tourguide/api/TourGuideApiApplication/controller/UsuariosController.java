package br.com.tourguide.api.TourGuideApiApplication.controller;

import br.com.tourguide.api.TourGuideApiApplication.domain.senha.DadosAlteracaoSenha;
import br.com.tourguide.api.TourGuideApiApplication.domain.senha.DadosEsqueciSenha;
import br.com.tourguide.api.TourGuideApiApplication.domain.senha.DadosRecuperacaoSenha;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.DadosFotoPerfil;
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
import java.util.Optional;

/**
 * Controller para o módulo de Usuarios, com listagem, criação, atualização e exclusão
 * Obs: Não é usado para a autenticação. Isso é feito pelo UserDetailsService no arquivo UsuarioService.java
 */
@RestController
@RequestMapping("api/usuario")
@AllArgsConstructor
public class UsuariosController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    /**
     * Lista todos os usuários cadastrados, com nome, email e perfil. Acesso restrito ao perfil ADMIN:
     * <ul>
     *   <li>O {@code @PreAuthorize} checa o perfil do usuário logado antes de executar o método;</li>
     *   <li>Se não for ADMIN, retorna 403;</li>
     *   <li>Converte cada {@code UsuarioEntity} em {@code UsuarioResponseDTO}, para não expor dados sensíveis como a senha.</li>
     * </ul>
     */
    @GetMapping("/lista")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> buscarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll().stream() //findAll ja é méthodo padrao do JPA.
                .map(UsuarioResponseDTO::new)
                .toList());
    }

    /**
     * Retorna dados do usuário logado, pegando do contexto salvo pelo filter:
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> buscarUsuarioDados(@AuthenticationPrincipal UsuarioEntity logado) {
        return ResponseEntity.ok(new UsuarioResponseDTO(logado));
    }

    /**
     * Adiciona ou troca a foto de perfil do usuário logado. É opcional, por isso fica separado do cadastro:
     * <ul>
     *   <li>Recebe a foto em base64 no campo {@code fotoBase64} (puro ou no formato data URI);</li>
     *   <li>Retorna 400 se o base64 for inválido, se não for PNG/JPEG ou se passar do tamanho máximo;</li>
     *   <li>Se der certo, salva a foto e retorna 204.</li>
     * </ul>
     */
    @PatchMapping("/me/foto")
    public ResponseEntity<Void> salvarFotoPerfil(@RequestBody @Valid DadosFotoPerfil dados, @AuthenticationPrincipal UsuarioEntity logado) {
        usuarioService.salvarFotoPerfil(dados, logado);
        return ResponseEntity.noContent().build();
    }

    // ----------- Mudança de senha -----------

    /**
     * Altera a senha do usuário logado:
     * <ul>
     *   <li>Confere se a senha atual informada bate com a senha salva (BCrypt), retornando 400 se não bater;</li>
     *   <li>Confere se a nova senha e a confirmação são iguais, retornando 400 se forem diferentes;</li>
     *   <li>Criptografa a nova senha com BCrypt, salva e retorna 204.</li>
     * </ul>
     */
    @PostMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenha(@RequestBody @Valid DadosAlteracaoSenha dados, @AuthenticationPrincipal UsuarioEntity logado) {
        usuarioService.alterarSenha(dados, logado);
        return ResponseEntity.noContent().build();
    }

    /**
     * Etapa 1 do Esqueci Senha: endpoint para enviar o email. Sempre retorna 202, exista o email ou não, para não revelar quem está cadastrado
     */
    @PostMapping("/esqueci-minha-senha")
    public ResponseEntity<Void> esqueciMinhaSenha(@RequestBody @Valid DadosEsqueciSenha dados) {
        usuarioService.enviarTokenRecuperacao(dados);
        return ResponseEntity.accepted().build();
    }

    /**
     * Etapa 2 do Esqueci Senha: endpoint usada ao clicar no email recebido para realmente recuperar a senha
     */
    @PostMapping("/recuperar-senha")
    public ResponseEntity<Void> recuperarSenha(@RequestBody @Valid DadosRecuperacaoSenha dados) {
        usuarioService.recuperarSenha(dados);
        return ResponseEntity.noContent().build();
    }

    // ----------- Autenticação de 2 fatores -----------

    /**
     * Etapa 1 da autenticação de dois fatores (A2F): gera o secret e devolve a URL do QR code:
     * <ul>
     *   <li>O TotpService gera um novo secret aleatório (Base32), que é salvo no usuário logado;</li>
     *   <li>Retorna 200 com a URL {@code otpauth://totp/...}, contendo o secret, o usuário e o emissor;</li>
     *   <li>O front transforma essa URL em imagem de QR code, que o usuário lê com o app autenticador
     *   (Google Authenticator, Authy, etc.);</li>
     *   <li>O A2F ainda não fica ativo aqui: isso só acontece no {@code /ativar-a2f}, depois de confirmar um código.</li>
     * </ul>
     */
    @PatchMapping("/configurar-a2f")
    public ResponseEntity<String> gerarQrCode(@AuthenticationPrincipal UsuarioEntity logado) {
        return ResponseEntity.ok(usuarioService.gerarQrCode(logado));
    }

    /**
     * Etapa 2 da autenticação de dois fatores (A2F): confirma o código e ativa o A2F do usuário logado:
     * <ul>
     *   <li>Recebe por parâmetro ({@code ?codigo=}) o código de 6 dígitos gerado pelo app autenticador após ler o QR code;</li>
     *   <li>Se o A2F já estiver ativo, retorna 400;</li>
     *   <li>O TotpService valida o código com o secret gerado no {@code configurar-a2f}, retornando 400 se for inválido;</li>
     *   <li>Se for válido, ativa o A2F e retorna 204. A partir daí, o login passa a exigir o código no {@code verificar-a2f}.</li>
     * </ul>
     * Confirmar o código antes de ativar garante que o usuário configurou o app corretamente, evitando que ele fique
     * sem conseguir logar.
     */
    @PatchMapping("/ativar-a2f")
    public ResponseEntity<Void> ativarA2f(@RequestParam String codigo, @AuthenticationPrincipal UsuarioEntity logado) {
        usuarioService.ativarA2f(codigo, logado);
        return ResponseEntity.noContent().build();
    }


}
