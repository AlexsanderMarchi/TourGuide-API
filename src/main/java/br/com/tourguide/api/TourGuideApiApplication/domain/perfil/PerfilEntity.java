package br.com.tourguide.api.TourGuideApiApplication.domain.perfil;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidade de perfis. Hoje temos dois perfis:
 *  <ul>
 *      <li>USUARIO - Acesso limitado</li>
 *      <li>ADMIN - Com acesso a tudo</li>
 *  </ul>
 */
@Entity
@Table(name = "perfil")
@Getter
@NoArgsConstructor
public class PerfilEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;

}
