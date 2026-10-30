package br.com.tourguide.api.TourGuideApiApplication.domain.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.tourguide.api.TourGuideApiApplication.domain.perfil.PerfilEntity;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

// Usa o H2 em memoria com as migrations de src/test/resources/db/migration-h2, pois as do MySQL nao rodam no H2
@DataJpaTest(properties = "spring.flyway.locations=classpath:db/migration-h2")
class UsuarioRepositoryTest {

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private TestEntityManager em;


  @Test
  @DisplayName("Deveria retornar vazio quando não existe usuário")
  void findByNomeIgnoreCase1() {
    //given ou arrange - inserção de dados
    String nomeUsuario = "NaoExisteUsuario";

    //when ou act - a ação de fato
    Optional<UsuarioEntity> usuarioNaoExiste = usuarioRepository.findByNomeIgnoreCase(nomeUsuario);

    //then ou assert - resultado de fato
    assertThat(usuarioNaoExiste).isEmpty();
  }

  @Test
  @DisplayName("Deveria retornar um usuario existente")
  void findByNomeIgnoreCase2() {
    //given ou arrange - inserção de dados
    cadastrarUsuario("ExisteUsuario", "$2a$12$2I7SdLRpbW7blulvtU8kwu7AEDgXEDyLLh29.rI5HSBhf.2SNYNYq", 2L);
    String nomeUsuario = "existeusuario";

    //when ou act - a ação de fato
    Optional<UsuarioEntity> usuarioExiste = usuarioRepository.findByNomeIgnoreCase(nomeUsuario);

    //then ou assert - resultado de fato
    assertThat(usuarioExiste).isPresent();
    assertThat(usuarioExiste.get().getNome()).isEqualTo("ExisteUsuario");
  }

  // metodo para cadastrar no banco
  private void cadastrarUsuario(String nome, String senha, Long idPerfil) {
    var perfil = em.find(PerfilEntity.class, idPerfil);
    em.persist(new UsuarioEntity(nome, senha, perfil));
  }

}
