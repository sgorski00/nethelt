-- The deferred trigger would leave pending events that block the ALTER TABLE below
SET CONSTRAINTS trg_monitoring_results_delete_extension IMMEDIATE;

DELETE FROM monitoring_results r
USING monitoring_results older
WHERE r.task_id = older.task_id
    AND r.executed_at = older.executed_at
    AND r.id > older.id;

DROP INDEX idx_monitoring_results_task_id_executed_at;

ALTER TABLE monitoring_results ADD CONSTRAINT uq_monitoring_results_task_id_executed_at UNIQUE (task_id, executed_at);
