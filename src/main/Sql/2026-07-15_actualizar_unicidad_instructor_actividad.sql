-- Permite que un instructor dicte la misma actividad en horarios diferentes.
-- Conserva la protección contra una asignación exactamente duplicada.
-- Detecta el índice anterior por sus columnas, ya que Hibernate puede haberle
-- asignado un nombre distinto en cada base. Repetimos el bloque para cubrir
-- instalaciones que tengan tanto el índice original como uno autogenerado.

SET @indice_anterior = (
    SELECT INDEX_NAME
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'INSTRUCTOR_ACTIVIDAD'
      AND NON_UNIQUE = 0
    GROUP BY INDEX_NAME
    HAVING GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX)
           = 'ID_ACTIVIDAD,ID_INSTRUCTOR'
    LIMIT 1
);
SET @sql = IF(@indice_anterior IS NULL, 'SELECT 1', CONCAT(
    'DROP INDEX `', REPLACE(@indice_anterior, '`', '``'),
    '` ON INSTRUCTOR_ACTIVIDAD'
));
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @indice_anterior = (
    SELECT INDEX_NAME
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'INSTRUCTOR_ACTIVIDAD'
      AND NON_UNIQUE = 0
    GROUP BY INDEX_NAME
    HAVING GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX)
           = 'ID_ACTIVIDAD,ID_INSTRUCTOR'
    LIMIT 1
);
SET @sql = IF(@indice_anterior IS NULL, 'SELECT 1', CONCAT(
    'DROP INDEX `', REPLACE(@indice_anterior, '`', '``'),
    '` ON INSTRUCTOR_ACTIVIDAD'
));
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

CREATE UNIQUE INDEX IF NOT EXISTS UK_DICTA_ASIGNACION
    ON INSTRUCTOR_ACTIVIDAD (ID_ACTIVIDAD, ID_INSTRUCTOR, DIAS, HORARIO);
