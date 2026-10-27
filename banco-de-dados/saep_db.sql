-- =============================================================
-- SAEP - Controle de Estoque de Autopecas
-- Banco: saep_db | MySQL Community Server 8.4 LTS
-- Execute este arquivo no MySQL Workbench ou cliente MySQL.
-- Usuarios demonstrativos: admin/admin123, maria/maria123, joao/joao123
-- As senhas sao armazenadas como SHA-256 apenas para fins didaticos.
-- Para uso real, prefira Argon2id ou BCrypt e politicas de senha robustas.
-- =============================================================

CREATE DATABASE IF NOT EXISTS saep_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE saep_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS movimentacoes;
DROP TABLE IF EXISTS produtos;
DROP TABLE IF EXISTS usuarios;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    senha_hash CHAR(64) NOT NULL,
    ativo TINYINT(1) NOT NULL DEFAULT 1,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_usuarios_ativo CHECK (ativo IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE produtos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nome VARCHAR(120) NOT NULL,
    marca VARCHAR(80) NOT NULL,
    modelo VARCHAR(80) NOT NULL,
    ano_fabricacao SMALLINT NOT NULL,
    compatibilidade VARCHAR(180) NOT NULL,
    quantidade INT NOT NULL DEFAULT 0,
    estoque_minimo INT NOT NULL DEFAULT 0,
    ativo TINYINT(1) NOT NULL DEFAULT 1,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_produtos_quantidade CHECK (quantidade >= 0),
    CONSTRAINT chk_produtos_minimo CHECK (estoque_minimo >= 0),
    CONSTRAINT chk_produtos_ano CHECK (ano_fabricacao BETWEEN 1886 AND 2100),
    CONSTRAINT chk_produtos_ativo CHECK (ativo IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE movimentacoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    produto_id INT NOT NULL,
    usuario_id INT NOT NULL,
    tipo ENUM('ENTRADA', 'SAIDA') NOT NULL,
    quantidade INT NOT NULL,
    data_movimentacao DATETIME NOT NULL,
    observacao VARCHAR(255) NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_movimentacoes_quantidade CHECK (quantidade > 0),
    CONSTRAINT fk_movimentacoes_produto FOREIGN KEY (produto_id)
        REFERENCES produtos(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_movimentacoes_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_movimentacoes_data (data_movimentacao),
    INDEX idx_movimentacoes_produto (produto_id)
) ENGINE=InnoDB;

-- 3 usuarios de exemplo. Senhas: admin123, maria123 e joao123.
INSERT INTO usuarios (nome, usuario, senha_hash, ativo) VALUES
('Administrador do Sistema', 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 1),
('Maria Oliveira', 'maria', '626e3c805e77eeb472c42c6be607be2af7ac5c08fd7050f278e0330fe81abf57', 1),
('Joao Santos', 'joao', '8681d505cb1b0344d0c7aa3c0e655c7e3a5add0f0960c1d142070a64cacab031', 1);

-- 3 produtos demonstrativos. As quantidades correspondem ao historico inserido abaixo.
INSERT INTO produtos
(codigo, nome, marca, modelo, ano_fabricacao, compatibilidade, quantidade, estoque_minimo, ativo) VALUES
('VEL-001', 'Filtro de oleo', 'Fram', 'PH6017A', 2022, 'Honda Civic 1.8', 4, 5, 1),
('FRE-002', 'Pastilha de freio dianteira', 'Cobreq', 'N-1234', 2021, 'Volkswagen Gol 1.0', 2, 3, 1),
('IGN-003', 'Vela de ignicao', 'NGK', 'BKR6E', 2023, 'Chevrolet Onix 1.0', 15, 5, 1);

-- Historico de exemplo. Entradas e saidas somam o saldo atual de cada produto.
INSERT INTO movimentacoes
(produto_id, usuario_id, tipo, quantidade, data_movimentacao, observacao) VALUES
(1, 1, 'ENTRADA', 6, DATE_SUB(NOW(), INTERVAL 6 DAY), 'Recebimento inicial de mercadoria'),
(1, 2, 'SAIDA', 2, DATE_SUB(NOW(), INTERVAL 3 DAY), 'Atendimento de pedido'),
(2, 1, 'ENTRADA', 3, DATE_SUB(NOW(), INTERVAL 5 DAY), 'Recebimento inicial de mercadoria'),
(2, 3, 'SAIDA', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'Venda no balcao'),
(3, 1, 'ENTRADA', 20, DATE_SUB(NOW(), INTERVAL 4 DAY), 'Recebimento de lote'),
(3, 2, 'SAIDA', 5, DATE_SUB(NOW(), INTERVAL 1 DAY), 'Atendimento de pedido');

-- Conferencias uteis apos executar o script:
SELECT id, nome, usuario, ativo FROM usuarios ORDER BY id;
SELECT codigo, nome, quantidade, estoque_minimo FROM produtos WHERE ativo = 1 ORDER BY nome;
SELECT m.id, p.nome AS produto, m.tipo, m.quantidade, m.data_movimentacao, u.nome AS responsavel
FROM movimentacoes m JOIN produtos p ON p.id = m.produto_id
JOIN usuarios u ON u.id = m.usuario_id ORDER BY m.data_movimentacao DESC;
