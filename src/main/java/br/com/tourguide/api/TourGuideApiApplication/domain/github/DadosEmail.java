package br.com.tourguide.api.TourGuideApiApplication.domain.github;

public record DadosEmail(String email,
                         Boolean primary,
                         Boolean verified,
                         String visibility) {
}
