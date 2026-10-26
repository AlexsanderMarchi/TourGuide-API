ALTER TABLE usuario
    ADD COLUMN email VARCHAR(255) NULL,
    ADD COLUMN recuperacao_token_senha VARCHAR(255) NULL,
    ADD COLUMN expiracao_token_senha DATETIME NULL,
    ADD CONSTRAINT uk_usuario_email UNIQUE (email),
    ADD CONSTRAINT uk_usuario_recuperacao_token_senha UNIQUE (recuperacao_token_senha);
