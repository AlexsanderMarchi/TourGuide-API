-- Schema consolidado (equivalente as migrations V1 a V5 do MySQL) em SQL compativel com o H2, usado nos testes
CREATE TABLE perfil(
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL UNIQUE,

    PRIMARY KEY (id)
);

INSERT INTO perfil (nome) VALUES ('ADMIN'), ('USUARIO');

CREATE TABLE usuario(
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    perfil BIGINT NOT NULL,
    email VARCHAR(255),
    recuperacao_token_senha VARCHAR(255),
    expiracao_token_senha TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_nome UNIQUE (nome),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT uk_usuario_recuperacao_token_senha UNIQUE (recuperacao_token_senha),
    CONSTRAINT fk_usuario_perfil FOREIGN KEY (perfil) REFERENCES perfil(id)
);
