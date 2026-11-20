-- Run manually after selecting the application database (USE database_name).
-- Hibernate schema update does not reliably remove existing unique indexes.
-- Preserve ticket history and foreign keys; only drop the exact two-column uniqueness.

SET @ticket_unique_drops = (
    SELECT GROUP_CONCAT(
        CONCAT('DROP INDEX `', REPLACE(index_name, '`', '``'), '`') SEPARATOR ', '
    )
    FROM (
        SELECT index_name
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'tickets'
          AND non_unique = 0
          AND index_name <> 'PRIMARY'
        GROUP BY index_name
        HAVING COUNT(*) = 2
           AND SUM(column_name = 'seat_id') = 1
           AND SUM(column_name = 'showtime_id') = 1
    ) AS matching_indexes
);

-- Provide a non-unique replacement before removing an index backing a foreign key.
SET @ticket_history_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'tickets'
      AND index_name = 'idx_tickets_seat_showtime_history'
);
SET @ticket_index_sql = IF(
    @ticket_unique_drops IS NOT NULL AND @ticket_history_index_exists = 0,
    'ALTER TABLE tickets ADD INDEX idx_tickets_seat_showtime_history (seat_id, showtime_id)',
    'SELECT ''No replacement index required'' AS migration_status'
);
PREPARE ticket_index_statement FROM @ticket_index_sql;
EXECUTE ticket_index_statement;
DEALLOCATE PREPARE ticket_index_statement;

SET @ticket_drop_sql = IF(
    @ticket_unique_drops IS NULL,
    'SELECT ''No matching unique index found'' AS migration_status',
    CONCAT('ALTER TABLE tickets ', @ticket_unique_drops)
);
PREPARE ticket_drop_statement FROM @ticket_drop_sql;
EXECUTE ticket_drop_statement;
DEALLOCATE PREPARE ticket_drop_statement;