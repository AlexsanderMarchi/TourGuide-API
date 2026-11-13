package br.com.tourguide.api.TourGuideApiApplication.domain.usuario;

import br.com.tourguide.api.TourGuideApiApplication.domain.senha.DadosAlteracaoSenha;
import br.com.tourguide.api.TourGuideApiApplication.domain.senha.DadosEsqueciSenha;
import br.com.tourguide.api.TourGuideApiApplication.domain.senha.DadosRecuperacaoSenha;
import br.com.tourguide.api.TourGuideApiApplication.infra.email.EmailService;
import br.com.tourguide.api.TourGuideApiApplication.infra.totp.TotpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

/**
 * Service para:
 * <ul>
 *     <li>Buscar usuario no banco MySql. O implements {@code UserDetailsService} que faz essa conexão automatica.
 *     Outros metodos nesse service são relacionado as senhas, que é para alterar, esqueci e recuperar senha</li>
 *     <li>Validar regras de mudanças de senha</li>
 * </ul>
 *
 */
@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encriptador;
    private final EmailService emailService;
    private final TotpService totpService;

    private static final long MINUTOS_VALIDADE_EMAIL_TOKEN = 30;

    // Limite de 20 MB do texto em base64 (~15 MB de imagem). A coluna foto_perfil é LONGTEXT (V8), que comporta esse tamanho
    // (o MEDIUMTEXT vai só até 16 MB e o TEXT até 64 KB)
    private static final int TAMANHO_MAXIMO_FOTO = 20 * 1024 * 1024;

    /**
     * metodo que valida o usuario
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario não econtradoo"));
    }

    /**
     * metodo para alterar senha, no cenário em que o usuário trocou a senha ja estando logado
     */
    public void alterarSenha(DadosAlteracaoSenha dados, UsuarioEntity logado) {
        if (!encriptador.matches(dados.senhaAtual(), logado.getPassword())) {
            throw new IllegalArgumentException("Senha digitada não confere com a senha atual!");
        }

        if (!dados.novaSenha().equals(dados.novaSenhaConfirmacao())) {
            throw new IllegalArgumentException("Senha e confirmação não conferem!");
        }

        String senhaCriptografada = encriptador.encode(dados.novaSenha());
        logado.alterarSenha(senhaCriptografada);
        usuarioRepository.save(logado);
    }

    /**
     * Gera um token de recuperação e manda o link por email.
     * Se o email não existir, não faz nada e não lança erro: assim ninguém descobre quais emails estão cadastrados.
     */
    public void enviarTokenRecuperacao(DadosEsqueciSenha dados) {
        usuarioRepository.findByEmailIgnoreCase(dados.email()).ifPresent(usuario -> {
            String token = usuario.gerarTokenRecuperacao(MINUTOS_VALIDADE_EMAIL_TOKEN);
            usuarioRepository.save(usuario);
            emailService.enviarEmailRecuperacaoSenha(usuario.getEmail(), token, MINUTOS_VALIDADE_EMAIL_TOKEN);
        });
    }

    /**
     * Troca a senha a partir do token recebido por email. O token só vale uma vez e é apagado ao trocar a senha
     */
    public void recuperarSenha(DadosRecuperacaoSenha dados) {
        UsuarioEntity usuario = usuarioRepository.findByTokenRecuperacao(dados.token())
                .filter(u -> !u.tokenRecuperacaoExpirado())
                .orElseThrow(() -> new IllegalArgumentException("Link de recuperação inválido ou expirado!"));

        if (!dados.novaSenha().equals(dados.novaSenhaConfirmacao())) {
            throw new IllegalArgumentException("Senha e confirmação não conferem!");
        }

        usuario.alterarSenha(encriptador.encode(dados.novaSenha()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public String gerarQrCode(UsuarioEntity logado) {
        var secret = totpService.gerarSecret();
        logado.gerarSecret(secret);
        usuarioRepository.save(logado);

        return totpService.gerarQrCode(logado);
    }

    @Transactional
    public void ativarA2f(String codigo, UsuarioEntity logado) {
        if(logado.isA2fAtiva()) {
            throw new IllegalArgumentException("Sua autenticação de dois fatores já está ativada!");
        }

        var codigoValido = totpService.verificarCodigo(codigo, logado);

        if(!codigoValido) {
            throw new IllegalArgumentException("Código inválido!");
        }

        logado.ativarA2f();
        usuarioRepository.save(logado);
    }

    /**
     * Salva a foto de perfil do usuário logado:
     * <ul>
     *   <li>Aceita o base64 puro ou no formato data URI, removendo o prefixo "data:...;base64," se vier;</li>
     *   <li>Decodifica o base64, retornando 400 se for inválido;</li>
     *   <li>Confere pelos primeiros bytes se é PNG ou JPEG de verdade (e não só um texto com outra extensão);</li>
     *   <li>Salva no formato data URI, que o front usa direto no {@code <img src>}, respeitando o tamanho da coluna.</li>
     * </ul>
     */
    @Transactional
    public void salvarFotoPerfil(DadosFotoPerfil dados, UsuarioEntity logado) {
        String fotoBase64 = dados.fotoBase64().trim();
        if (fotoBase64.startsWith("data:") && fotoBase64.contains(",")) {
            fotoBase64 = fotoBase64.substring(fotoBase64.indexOf(',') + 1);
        }

        byte[] bytesFoto;
        try {
            bytesFoto = Base64.getDecoder().decode(fotoBase64);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("A foto não está em base64 válido!");
        }

        String tipoImagem = identificarTipoImagem(bytesFoto);
        if (tipoImagem == null) {
            throw new IllegalArgumentException("A foto deve ser PNG ou JPEG!");
        }

        String fotoDataUri = "data:" + tipoImagem + ";base64," + fotoBase64;
        if (fotoDataUri.length() > TAMANHO_MAXIMO_FOTO) {
            throw new IllegalArgumentException("A foto é muito grande! O tamanho máximo é de aproximadamente 15 MB.");
        }

        logado.salvarFoto(fotoDataUri);
        usuarioRepository.save(logado);
    }

    /**
     * Identifica o tipo da imagem pela assinatura (magic bytes) do arquivo: PNG começa com 89 50 4E 47 e JPEG com FF D8 FF
     */
    private String identificarTipoImagem(byte[] bytes) {
        if (bytes.length >= 4 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
            return "image/png";
        }
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        return null;
    }
}
