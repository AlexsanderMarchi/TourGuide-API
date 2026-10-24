package br.com.tourguide.api.TourGuideApiApplication.domain.usuario;

import lombok.Data;
import lombok.NoArgsConstructor;

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
