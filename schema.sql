CREATE TABLE IF NOT EXISTS usuario (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    intentos_fallidos INTEGER DEFAULT 0,
    estado VARCHAR(20) DEFAULT 'ACTIVO'
);
