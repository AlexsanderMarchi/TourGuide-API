package br.com.tourguide.api.TourGuideApiApplication.domain.usuario;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service para buscar usuario no banco MySql. O implements UserDetailsService que faz essa conexão automatica
 */
@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByNomeIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario não econtradoo"));
    }
}
