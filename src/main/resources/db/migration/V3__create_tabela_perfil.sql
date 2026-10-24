CREATE TABLE perfil(
        id bigint NOT NULL auto_increment,
        nome VARCHAR(100) NOT NULL UNIQUE,

        PRIMARY KEY (id)
);

ALTER TABLE usuario
    ADD COLUMN perfil BIGINT,
    ADD CONSTRAINT fk_usuario_perfil
        FOREIGN KEY (perfil) REFERENCES perfil(id);