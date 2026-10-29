## 🗺️ TourGuide API

The goal of this project is to practice and learn infrastructure tools and services, including authentication, CI/CD,
database integration, and Docker.

The project is a travel guide and planning API designed to simplify backend infrastructure and provide the data and
functionality required by the frontend.

---

### 🛠️ Technologies

| Category       | Tool                                                                                 |
|----------------|--------------------------------------------------------------------------------------|
| Language       | Java 21                                                                              |
| Framework      | Spring Boot 4.1 (Web MVC, Data JPA, Security)                                        |
| Autenticação   | Spring Security + JWT ([auth0 java-jwt](https://github.com/auth0/java-jwt)) + BCrypt |
| Database       | MySQL                                                                                |
| ORM            | Spring Data JPA (Hibernate)                                                          |
| Migrations     | Flyway                                                                               |
| Local database | H2 (ainda não configurado)                                                           |
| Build          | Maven                                                                                |
| Utils          | Lombok                                                                               |
| Containers     | Docker / Docker Compose                                                              |
| CI/CD          | GitHub Actions                                                                       |

---

### ⚙️ How it works

#### Authentication (JWT, stateless)

##### Login

1. We defined a configuration class where we customize Spring Security interfaces. In it, we configure the
   `SecurityFilterChain`, `AuthenticationManager` and `PasswordEncoder`. In the SecurityFilterChain, we define the main
   security configurations, such as setting the session policy to STATELESS, CSRF, the endpoints allowed without
   authentication, and the endpoints that require authentication. The AuthenticationManager is used to look up user
   credentials in the database and compare them with the ones sent to the endpoint. The PasswordEncoder is required to
   define how passwords are encoded (if we mock a user credential, the password must be prefixed with `{noop}`).
2. We will use a database to store the users, so we created `UsuarioEntity` implementing `UserDetails` (the Spring
   Security interface for authentication). We also created `UsuarioRepository` and `UsuarioService` to work with JPA.
   In UsuarioService, we implement the Spring Security interface `UserDetailsService`, which provides the method
   Spring uses to load users during login: `loadUserByUsername`.
3. For the login endpoint, we have the `AutenticacaoController`, which calls the AuthenticationManager to authenticate
   the
   credentials. Since the API is stateless, after authentication we generate a JWT in the `TokenService`, containing the
   user data and the expiration date. The returned token can then be stored on the front end and sent with every
   request. The TokenService uses Auth0’s JWT library and has two methods: one to generate the token and another to
   validate it.

##### Requests persistence

1. After login, we need to persist the authentication in subsequent requests. To do that, we store the token on the
   frontend and send it with every request. On the API side, this validation is handled by a filter, which intercepts
   every HTTP request and applies some logic to it. We created our own filter, `SecurityFilter`, to validate the token.
   Since Spring has its own filters, we need to run SecurityFilter before them. To do that, we call the addFilterBefore
   method in the SecurityFilterChain.
2. SecurityFilter uses the validation method from TokenService. If the request doesn’t have a token, the filter simply
   passes it along the chain, and Spring handles it through the rules defined in `authorizeHttpRequests` method.

### Profiles and Authorization

1. For user profiles, we created a new entity, `PerfilEntity`, which is referenced in UsuarioEntity.
2. For authorization, the UserDetails interface already provides the getAuthorities method, which Spring Security uses
   to determine the user’s permissions. We implemented it using PerfilEntity.
3. To define which endpoints a user can access based on their profile, we use the `@PreAuthorize` annotation in the
   controllers. To enable it, we first need to add the `@EnableMethodSecurity` annotation to the ConfigurationSecurity
   class.

### Respostas de erro

---

## 📡 Endpoints

| Método | Rota                 | Autenticação | Descrição                         |
|--------|----------------------|--------------|-----------------------------------|
| `POST` | `/api/auth/login`    | Pública      | Faz o login e retorna o JWT       |
| `POST` | `/api/auth/logout`   | Autenticada  | Logout                            |
| `GET`  | `/api/usuario/lista` | `ADMIN`      | Lista os usuários com seus perfis |

## 🗄️ Banco de dados

---

## 🚀 Como rodar

### Pré-requisitos

- Java 21
- MySQL 8 rodando em `localhost:3306` com um banco `tour_guide_db`
- (Opcional) Docker

### Localmente

### Variáveis de ambiente

### Com Docker Compose (API + front)

## 🔄 CI/CD

## 📁 Estrutura

```
src/main/java/.../TourGuideApiApplication
├── controller/          # Endpoints REST (login, usuários)
├── domain/
│   ├── usuario/         # Entidade, repositório, service e DTOs de usuário
│   └── perfil/          # Entidade de perfil
└── infra/
    ├── security/        # Configuração do Spring Security, filtro e serviço de JWT
    └── errors_validation/  # Tratamento global de erros
```

---

## 📌 Próximos passos

- [ ] Cadastro de usuários
- [ ] Usar Redis <!-- conforme o TODO do AutenticacaoController -->
- [ ] Banco H2 para desenvolvimento e testes
- [ ] Template de PR

<!-- adicione o que mais estiver planejado -->

---

## 👤 Autor

**Alexsander Marchi** <!-- confira o nome -->
[GitHub](https://github.com/AlexsanderMarchi) · [LinkedIn](https://www.linkedin.com/in/SEU-LINKEDIN)
