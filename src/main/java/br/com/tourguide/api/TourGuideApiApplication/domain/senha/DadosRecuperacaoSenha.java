package br.com.tourguide.api.TourGuideApiApplication.domain.senha;

import jakarta.validation.constraints.NotBlank;

public record DadosRecuperacaoSenha(@NotBlank String token, @NotBlank String novaSenha,
                                    @NotBlank String novaSenhaConfirmacao) {

}
