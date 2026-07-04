USE warehouse_twin;
ALTER TABLE warehouse_event ADD COLUMN actor_type VARCHAR(32) NULL AFTER object_type;
