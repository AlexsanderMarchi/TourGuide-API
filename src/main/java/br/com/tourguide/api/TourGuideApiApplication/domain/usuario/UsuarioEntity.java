package br.com.tourguide.api.TourGuideApiApplication.domain.usuario;

import br.com.tourguide.api.TourGuideApiApplication.domain.perfil.PerfilEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name="usuario")
@Getter
public class UsuarioEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private  String senha;

    /** Linkagem de tabela perfil ao usuario. ManyToOne pq é muitos usuarios para um perfil */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "perfil", nullable = false)
    private PerfilEntity perfil;

    /** O prefixo "ROLE_" permite usar perfil como @PreAuthorize */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.getNome()));
    }

    @Override
    public @Nullable String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return nome;
    }
}
