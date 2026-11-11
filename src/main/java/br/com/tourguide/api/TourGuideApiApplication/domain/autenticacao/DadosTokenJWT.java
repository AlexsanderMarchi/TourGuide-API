package br.com.tourguide.api.TourGuideApiApplication.domain.autenticacao;

public record DadosTokenJWT(String tokenJWT, String refreshTokenJWT, Boolean a2f) {
}
