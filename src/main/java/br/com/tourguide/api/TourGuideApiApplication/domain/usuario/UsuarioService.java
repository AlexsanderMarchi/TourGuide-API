package br.com.tourguide.api.TourGuideApiApplication.domain.usuario;

import br.com.tourguide.api.TourGuideApiApplication.infra.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service para buscar usuario no banco MySql. O implements UserDetailsService que faz essa conexão automatica
 * Outros metodos nesse service são relacionado as senhas, que é para alterar, esqueci e recuperar senha
 */
@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encriptador;
    private final EmailService emailService;

    private static final long MINUTOS_VALIDADE_TOKEN = 30;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByNomeIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario não econtradoo"));
    }

    public void alterarSenha(DadosAlteracaoSenha dados, UsuarioEntity logado) {
        if(!encriptador.matches(dados.senhaAtual(), logado.getPassword())) {
            throw new IllegalArgumentException("Senha digitada não confere com a senha atual!");
        }

        if(!dados.novaSenha().equals(dados.novaSenhaConfirmacao())) {
            throw new IllegalArgumentException("Senha e confirmação não conferem!");
        }

        String senhaCriptografada = encriptador.encode(dados.novaSenha());
        logado.alterarSenha(senhaCriptografada);
        usuarioRepository.save(logado);

    }

    /** Gera um token de recuperação e manda o link por email.
     * Se o email não existir, não faz nada e não lança erro: assim ninguém descobre quais emails estão cadastrados.
     */
    public void enviarTokenRecuperacao(DadosEsqueciSenha dados) {
        usuarioRepository.findByEmailIgnoreCase(dados.email()).ifPresent(usuario -> {
            String token = usuario.gerarTokenRecuperacao(MINUTOS_VALIDADE_TOKEN);
            usuarioRepository.save(usuario);
            emailService.enviarEmailRecuperacaoSenha(usuario.getEmail(), token, MINUTOS_VALIDADE_TOKEN);
        });
    }

    /** Troca a senha a partir do token recebido por email. O token só vale uma vez e é apagado ao trocar a senha */
    public void recuperarSenha(DadosRecuperacaoSenha dados) {
        var usuario = usuarioRepository.findByTokenRecuperacao(dados.token())
                .filter(u -> !u.tokenRecuperacaoExpirado())
                .orElseThrow(() -> new IllegalArgumentException("Link de recuperação inválido ou expirado!"));

        if(!dados.novaSenha().equals(dados.novaSenhaConfirmacao())) {
            throw new IllegalArgumentException("Senha e confirmação não conferem!");
        }

        usuario.alterarSenha(encriptador.encode(dados.novaSenha()));
        usuarioRepository.save(usuario);
    }
}
