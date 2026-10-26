package br.com.tourguide.api.TourGuideApiApplication.domain.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByNomeIgnoreCase(String nome);
    Optional<UsuarioEntity> findByEmailIgnoreCase(String email);
    Optional<UsuarioEntity> findByTokenRecuperacao(String tokenRecuperacao);
}
