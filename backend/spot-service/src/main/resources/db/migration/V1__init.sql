-- RECON-812: migracion baseline de Flyway, sustituye al schema.sql que se
-- montaba como init script de Postgres. Debe reflejar exactamente
-- SpotEntity.
CREATE TABLE spots (
    id     UUID PRIMARY KEY,
    type   VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL
);
