-- ASY-07: Añadir columna para registrar el timestamp del último cambio de estado de la plaza (idempotencia por timestamp).
-- NOTA: La columna es NULLABLE por diseño. Las filas existentes quedarán con NULL (comportamiento fail-open), 
-- garantizando que no se descarten eventos en vuelo emitidos antes o durante el despliegue.
ALTER TABLE spots ADD COLUMN last_status_change_at TIMESTAMP WITH TIME ZONE;
