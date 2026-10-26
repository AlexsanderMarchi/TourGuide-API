package br.com.tourguide.api.TourGuideApiApplication.domain.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service para buscar usuario no banco MySql. O implements UserDetailsService que faz essa conexão automatica
 */
@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encriptador;

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
}
