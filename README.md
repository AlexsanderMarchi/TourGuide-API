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

## ⚙️ Como funciona

### Autenticação (JWT, stateless)

1. O cliente ...
2.

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
