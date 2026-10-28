package br.com.tourguide.api.TourGuideApiApplication.domain.usuario;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO do modulo de usuarios. Sem vinculo com autenticação.
 */
@Data
@NoArgsConstructor
public class UsuarioResponseDTO {

    private String nome;
    private String perfil;

    public UsuarioResponseDTO(UsuarioEntity usuario) {
        this.nome = usuario.getUsername();
        this.perfil = usuario.getPerfil().getNome();
    }
}
