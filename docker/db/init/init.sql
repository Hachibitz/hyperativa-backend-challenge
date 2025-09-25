-- Garante que estamos usando o banco de dados correto
USE hyperativa_db;

-- [CORREÇÃO] Cria as tabelas necessárias se elas não existirem.
-- O Hibernate (ddl-auto: update) irá então verificar e atualizar se necessário.

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Agora, insere os dados, que é a responsabilidade principal deste script.
-- A sintaxe INSERT IGNORE evita erros se os dados já existirem.

INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_USER');
INSERT IGNORE INTO roles (id, name) VALUES (2, 'ROLE_ADMIN');

-- Inserir o usuário de teste 'testuser' com a senha 'password'
INSERT IGNORE INTO users (id, username, email, password, enabled) VALUES (1, 'testuser', 'test@hyperativa.com', '$2a$10$dXJ3SW6G7P5aQV9mmUWcGeUvGzgVfxAbU6DFICHtlVBEad02/XoJK', true);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (1, 1);

-- Inserir usuário ADMIN
INSERT IGNORE INTO users (id, username, email, password, enabled) VALUES (2, 'admin', 'admin@hyperativa.com', '$2a$10$f6/X5L9v.Y/iJz2O8C8SNOuSgD5a550a2SkvQ/8bX.VI8sY2fHhG6', true);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (2, 2);