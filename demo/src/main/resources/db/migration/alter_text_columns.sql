-- Script para alterar colunas de texto para suportar textos longos
-- Execute este script no seu banco de dados MySQL

-- Alterar coluna 'text' na tabela 'answer' para TEXT
ALTER TABLE answer MODIFY COLUMN text TEXT;

-- Alterar coluna 'feedback' na tabela 'correction' para TEXT
ALTER TABLE correction MODIFY COLUMN feedback TEXT;

-- Alterar coluna 'description' na tabela 'question' para TEXT
ALTER TABLE question MODIFY COLUMN description TEXT;

-- Alterar coluna 'portuguese_translation' na tabela 'question' para TEXT
ALTER TABLE question MODIFY COLUMN portuguese_translation TEXT;




