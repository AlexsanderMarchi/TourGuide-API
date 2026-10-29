package br.com.tourguide.api.TourGuideApiApplication.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import br.com.tourguide.api.TourGuideApiApplication.domain.perfil.PerfilEntity;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioEntity;
import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// Usa o H2 em memoria com as migrations de src/test/resources/db/migration-h2, pois as do MySQL nao rodam no H2
@SpringBootTest(properties = "spring.flyway.locations=classpath:db/migration-h2")
@AutoConfigureTestDatabase // Diferente do @DataJpaTest, o @SpringBootTest usaria o MySQL do application.properties. Isso força o H2
@AutoConfigureMockMvc // Cria o MockMvc para simular as requisições HTTP sem subir o servidor
@Transactional // Desfaz o que cada teste inseriu no banco ao final dele
class UsuariosControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private EntityManager em;

  @Test
  @DisplayName("Deveria retornar lista de usuários quando o usuário logado é ADMIN")
  @WithMockUser(roles = "ADMIN") // Simula um usuário logado com o perfil ADMIN, sem precisar de token JWT
  void buscarUsuarios1() throws Exception {
    //given ou arrange - inserção de dados
    cadastrarUsuario("ExisteUsuario1", "$2a$12$2I7SdLRpbW7blulvtU8kwu7AEDgXEDyLLh29.rI5HSBhf.2SNYNYq", 2L);
    cadastrarUsuario("ExisteUsuario2", "$2a$12$2I7SdLRpbW7blulvtU8kwu7AEDgXEDyLLh29.rI5HSBhf.2SNYNYq", 2L);

    //when ou act - a ação de fato
    var response = mvc.perform(get("/api/usuario/lista"))
        .andReturn().getResponse();

    //then ou assert - resultado de fato
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).contains("ExisteUsuario1", "ExisteUsuario2");
  }

  @Test
  @DisplayName("Deveria retornar 403 quando o usuário logado não é ADMIN")
  @WithMockUser(roles = "USUARIO")
  void buscarUsuarios2() throws Exception {
    //when ou act - a ação de fato
    var response = mvc.perform(get("/api/usuario/lista"))
        .andReturn().getResponse();

    //then ou assert - resultado de fato
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
  }

  @Test
  @DisplayName("Deveria retornar 401 quando não há usuário logado")
  void buscarUsuarios3() throws Exception {
    //when ou act - a ação de fato
    var response = mvc.perform(get("/api/usuario/lista"))
        .andReturn().getResponse();

    //then ou assert - resultado de fato
    assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
  }

  // metodo para cadastrar no banco
  private void cadastrarUsuario(String nome, String senha, Long idPerfil) {
    var perfil = em.find(PerfilEntity.class, idPerfil); // 2 = USUARIO, inserido pela migration
    usuarioRepository.save(new UsuarioEntity(nome, senha, perfil));
  }
}
