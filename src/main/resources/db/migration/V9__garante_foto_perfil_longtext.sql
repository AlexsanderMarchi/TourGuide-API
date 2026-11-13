-- A V8 já alterava para LONGTEXT, mas a coluna voltou a ser varchar(255) no banco local.
-- Reaplica o tipo; o @Column(columnDefinition = "LONGTEXT") na UsuarioEntity mantém o Hibernate alinhado.
ALTER TABLE usuario
    MODIFY foto_perfil LONGTEXT NULL;
