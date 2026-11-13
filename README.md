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

### ⚙️ How it works (Authentication JWT, stateless)

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

##### Github Login

1. ...

##### Two-factor authentication

1. For additional security, we offer optional 2FA (two-factor authentication). `UsuariosController` has an
   endpoint that generates the QR code, using the Atlassian library to create the secret, which is stored in
   the database.
2. After the front end gets the QR code URL and the user registers it in their authenticator app, a second
   endpoint must be called to activate 2FA. This saves the activated status in the database.
3. From then on, every login requires a second step with the authentication code. 2FA does not apply to the
   GitHub login option.

##### Requests validation and persistence

1. After login, we need to persist the authentication in subsequent requests. To do that, we store the token on the
   frontend and send it with every request. On the API side, this validation is handled by a filter, which intercepts
   every HTTP request and applies some logic to it. We created our own filter, `SecurityFilter`, to validate the token.
   Since Spring has its own filters, we need to run SecurityFilter before them. To do that, we call the addFilterBefore
   method in the SecurityFilterChain.
2. SecurityFilter uses the validation method from TokenService. If the request doesn’t have a token, the filter simply
   passes it along the chain, and Spring handles it through the rules defined in `authorizeHttpRequests` method.
3. To persist token validation, we use a refresh token that is returned at login along with the access token. Both are
   stored on the front end, but the refresh token is only used when the access token expires. When this happens, the
   refresh token, which has a longer expiration time (2 hours), is validated, and the endpoint returns a new access
   token and refresh token.

##### Profiles and Authorization

1. For user profiles, we created a new entity, `PerfilEntity`, which is referenced in UsuarioEntity.
2. For authorization, the UserDetails interface already provides the getAuthorities method, which Spring Security uses
   to determine the user’s permissions. We implemented it using PerfilEntity.
3. To define which endpoints a user can access based on their profile, we use the `@PreAuthorize` annotation in the
   controllers. To enable it, we first need to add the `@EnableMethodSecurity` annotation to the ConfigurationSecurity
   class.

##### Password changes

1. ...

---

### 📡 Endpoints

| Método  | Rota                                | Autenticação | Descrição                                                                         |
|---------|-------------------------------------|--------------|-----------------------------------------------------------------------------------|
| `POST`  | `/api/auth/login`                   | Pública      | Faz o login e devolve o JWT e o refresh token em cookies (ou pede o código A2F)   |
| `POST`  | `/api/auth/login/verificar-a2f`     | Pública      | Valida o código do app autenticador e devolve os tokens em cookies                |
| `POST`  | `/api/auth/login/atualizar-token`   | Pública      | Usa o cookie do refresh token para gerar um novo JWT e refresh token              |
| `GET`   | `/api/auth/login/github`            | Pública      | Redireciona para a tela de autorização do GitHub (OAuth)                          |
| `GET`   | `/api/auth/login/github/autorizado` | Pública      | Callback do GitHub: busca o email do usuário e devolve os tokens                  |
| `POST`  | `/api/auth/logout`                  | Pública      | Faz o logout apagando os cookies dos tokens                                       |
| `GET`   | `/api/usuario/lista`                | `ADMIN`      | Lista os usuários com seus perfis                                                 |
| `POST`  | `/api/usuario/alterar-senha`        | Autenticada  | Altera a senha do usuário logado                                                  |
| `POST`  | `/api/usuario/esqueci-minha-senha`  | Pública      | Envia por email o token de recuperação de senha                                   |
| `POST`  | `/api/usuario/recuperar-senha`      | Pública      | Redefine a senha usando o token recebido por email                                |
| `PATCH` | `/api/usuario/configurar-a2f`       | Autenticada  | Gera o secret do A2F e devolve a URL do QR code                                   |
| `PATCH` | `/api/usuario/ativar-a2f`           | Autenticada  | Confirma o código do app autenticador e ativa o A2F                               |

### 🗄️ Banco de dados

- 

### 🚀 Como rodar

### Pré-requisitos

- Java 21
- MySQL 8 rodando em `localhost:3306` com um banco `tour_guide_db`
- (Opcional) Docker

### Localmente

- 

### Variáveis de ambiente

-

### Com Docker Compose (API + front)

-

### 🔄 CI/CD

-

### 📁 Estrutura

```
src/main/java/.../TourGuideApiApplication
├── controller/             # Endpoints REST (login, login pelo GitHub, usuários)
├── domain/
│   ├── autenticacao/       # DTOs do login e do A2F
│   ├── github/             # Service e DTOs do login OAuth com o GitHub
│   ├── perfil/             # Entidade de perfil
│   ├── senha/              # DTOs de alterar, esqueci e recuperar senha
│   └── usuario/            # Entidade, repositório, service e DTOs de usuário
└── infra/
    ├── email/              # Envio de emails (recuperação de senha)
    ├── errors_validation/  # Tratamento global de erros
    ├── security/           # Spring Security, filtro, serviço de JWT e cookies dos tokens
    ├── springdoc/          # Configuração do Swagger / OpenAPI
    └── totp/               # Geração e validação dos códigos do A2F (TOTP)
```

---

### 📌 Próximos passos

- [ ] Finalizar endpoints dos usuários
- [ ] Finalizar validação de erros
- [ ] Finalizar testes
- [ ] Arrumar path do acces token para nao mandar dos api/auth/** 
- [ ] Documentar e testar oAuth do github
- [ ] Testar email em recuperar senha

---

### 👤 Autor

**Alexsander Marchi** <!-- confira o nome -->
[GitHub](https://github.com/AlexsanderMarchi) · [LinkedIn](https://www.linkedin.com/in/alexsander-marchi-zunino-226332170)
