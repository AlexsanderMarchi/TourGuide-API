package br.com.tourguide.api.TourGuideApiApplication.domain.autenticacao;

/**
 * Corpo da resposta do login. Os tokens não vão no corpo, e sim em cookies HttpOnly via a classe CookiesToken,
 */
public record DadosLogin(Boolean a2f) {
}
