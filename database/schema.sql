-- Ejecutar en Query Tool de pgAdmin conectado a VoluntiaDB.
-- Compatible con las tablas encontradas en la instalación local.
CREATE TABLE IF NOT EXISTS usuario (
 usuario_id SERIAL PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL,
 email VARCHAR(100) NOT NULL UNIQUE,
 contrasena VARCHAR(100) NOT NULL,
 rol VARCHAR(50) NOT NULL,
 ubicacion VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS voluntario (
 voluntario_id SERIAL PRIMARY KEY,
 usuario_id INTEGER NOT NULL UNIQUE REFERENCES usuario(usuario_id) ON DELETE CASCADE,
 edad INTEGER,
 habilidades VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS organizacion (
 organizacion_id SERIAL PRIMARY KEY,
 usuario_id INTEGER NOT NULL UNIQUE REFERENCES usuario(usuario_id) ON DELETE CASCADE,
 ruc VARCHAR(100),
 info_contacto VARCHAR(200)
);
CREATE TABLE IF NOT EXISTS campania (
 campania_id SERIAL PRIMARY KEY,
 titulo VARCHAR(150) NOT NULL,
 descripcion VARCHAR(255),
 ubicacion VARCHAR(255),
 fecha TIMESTAMP WITHOUT TIME ZONE,
 requisitos VARCHAR(255),
 cupos INTEGER,
 organizacion_id INTEGER REFERENCES organizacion(organizacion_id) ON DELETE SET NULL
);
CREATE TABLE IF NOT EXISTS inscripcion (
 inscripcion_id SERIAL PRIMARY KEY,
 voluntario_id INTEGER NOT NULL REFERENCES voluntario(voluntario_id) ON DELETE CASCADE,
 campania_id INTEGER NOT NULL REFERENCES campania(campania_id) ON DELETE CASCADE,
 estado VARCHAR(50)
);
CREATE TABLE IF NOT EXISTS reporte_impacto (
 reporte_impacto_id SERIAL PRIMARY KEY,
 campania_id INTEGER NOT NULL REFERENCES campania(campania_id) ON DELETE CASCADE,
 arboles_plantados INTEGER,
 residuos_colectados_kg NUMERIC,
 horas_voluntariado INTEGER
);
