package br.com.tourguide.api.TourGuideApiApplication.domain.usuario;

import br.com.tourguide.api.TourGuideApiApplication.domain.perfil.PerfilEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="usuario")
@Getter
public class UsuarioEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private  String senha;
    private String email;

    /** Token enviado por email no "esqueci minha senha". Fica nulo quando não há recuperação em andamento */
    private String tokenRecuperacao;
    private LocalDateTime expiracaoToken;

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

    public void alterarSenha(String senhaCriptografada) {
        this.senha = senhaCriptografada;
        // Senha trocada invalida qualquer link de recuperação pendente
        this.tokenRecuperacao = null;
        this.expiracaoToken = null;
    }

    public String gerarTokenRecuperacao(long minutosValidade) {
        this.tokenRecuperacao = UUID.randomUUID().toString();
        this.expiracaoToken = LocalDateTime.now().plusMinutes(minutosValidade);
        return tokenRecuperacao;
    }

    public boolean tokenRecuperacaoExpirado() {
        return expiracaoToken == null || LocalDateTime.now().isAfter(expiracaoToken);
    }
}
