-- Versão de token por admin: incrementada no logout para revogar (no servidor)
-- todos os JWTs emitidos para aquele usuário.
ALTER TABLE admin_users ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;
