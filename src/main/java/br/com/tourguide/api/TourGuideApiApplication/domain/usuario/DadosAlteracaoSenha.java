package br.com.tourguide.api.TourGuideApiApplication.domain.usuario;

import jakarta.validation.constraints.NotBlank;

public record DadosAlteracaoSenha(@NotBlank String senhaAtual, @NotBlank String novaSenha, @NotBlank String novaSenhaConfirmacao) {

}
