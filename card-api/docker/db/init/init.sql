-- Este script será executado automaticamente pelo container do MySQL na primeira inicialização.

-- Cria as roles básicas se não existirem.
-- A aplicação pode tentar criar de novo, mas o ON CONFLICT garante que não haverá erro.
INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;

-- Cria o usuário de teste 'testuser' com a senha 'password'
-- O hash BCrypt abaixo corresponde a 'password'
INSERT INTO users (username, email, password, enabled) VALUES ('testuser', 'test@hyperativa.com', '$2a$10$dXJ3SW6G7P5aQV9mmUWcGeUvGzgVfxAbU6DFICHtlVBEad02/XoJK', true) ON CONFLICT (username) DO NOTHING;

-- Associa a role USER ao 'testuser'
INSERT INTO user_roles (user_id, role_id) VALUES ((SELECT id from users where username = 'testuser'), (SELECT id from roles where name = 'ROLE_USER')) ON CONFLICT DO NOTHING;

-- Cria um user admin
-- INSERT INTO users (username, email, password, enabled) VALUES ('admin', 'admin@hyperativa.com', '$2a$10$f6/X5L9v.Y/iJz2O8C8SNOuSgD5a550a2SkvQ/8bX.VI8sY2fHhG6', true) ON CONFLICT (username) DO NOTHING;
-- INSERT INTO user_roles (user_id, role_id) VALUES ((SELECT id from users where username = 'admin'), (SELECT id from roles where name = 'ROLE_ADMIN')) ON CONFLICT DO NOTHING;