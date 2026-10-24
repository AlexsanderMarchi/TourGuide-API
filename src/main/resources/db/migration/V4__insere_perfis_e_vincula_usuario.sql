INSERT INTO perfil (nome) VALUES ('ADMIN'), ('USUARIO');

UPDATE usuario
    SET perfil = (SELECT id FROM perfil WHERE nome = 'USUARIO')
    WHERE perfil IS NULL;

ALTER TABLE usuario MODIFY perfil BIGINT NOT NULL;
