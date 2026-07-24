-- DDL para el perfil prod (ddl-auto=validate): sin Flyway/Liquibase todavia,
-- el esquema se crea fuera de banda. Debe reflejar exactamente SpotEntity.
-- Se monta como init script en la Postgres dedicada de spot-service.

CREATE TABLE IF NOT EXISTS spots (
    id     UUID PRIMARY KEY,
    type   VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL
);
