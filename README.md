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

#### Autenticação (JWT, stateless)

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
3. There is the controller `AutenticacaoController` for the login. There we call AuthenticationManager to authenticate
   the credentials. After this, we create a jwt token in `TokenService` method, where it will return user data and
   expire date...

A API não guarda sessão (`STATELESS`): cada requisição se autentica só pelo token.

### Perfis e autorização

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
