package br.com.tourguide.api.TourGuideApiApplication.domain.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DadosEsqueciSenha(@NotBlank @Email String email) {

}
