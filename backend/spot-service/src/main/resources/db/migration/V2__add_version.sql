-- SpotEntity declara @Version Long version para el control de concurrencia
-- optimista, pero V1__init.sql nunca creo la columna. Sin esto Hibernate falla
-- al arrancar con ddl-auto=validate (application-prod.properties):
--
--   SchemaManagementException: Schema validation:
--   missing column [version] in table [spots]
--
-- Silencioso en dev y en CI: en dev ddl-auto=update crea la columna sola y CI
-- no arranca nunca en perfil prod, asi que solo se ve al levantar el stack.
--
-- A diferencia de vehicle y tariff, aqui la columna va NOT NULL: SpotEntity la
-- declara @Column(name = "version", nullable = false). DEFAULT 0 cubre las
-- filas que ya existan en las BD de demo; Hibernate asigna la version el solo
-- en cada INSERT posterior, asi que el default no se usa mas alla de la
-- propia migracion.
ALTER TABLE spots
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
