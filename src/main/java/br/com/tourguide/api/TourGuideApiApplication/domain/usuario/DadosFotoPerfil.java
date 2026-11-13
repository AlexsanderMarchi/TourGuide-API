package br.com.tourguide.api.TourGuideApiApplication.domain.usuario;

import jakarta.validation.constraints.NotBlank;

/**
 * Foto de perfil em base64. Aceita só o base64 puro ou no formato data URI ("data:image/png;base64,...")
 */
public record DadosFotoPerfil(@NotBlank String fotoBase64) {
}
