CREATE TABLE usuario(
    id bigint NOT NULL auto_increment,
    nome VARCHAR(100) NOT NULL,
    senha VARCHAR(6) NOT NULL UNIQUE,

    PRIMARY KEY (id)
);