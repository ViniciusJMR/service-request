ALTER TABLE solicitations ADD COLUMN priority VARCHAR(30);
ALTER TABLE solicitations ADD COLUMN preferred_date TIMESTAMP WITH TIME ZONE;
ALTER TABLE solicitations ADD COLUMN estimated_value DOUBLE PRECISION;
ALTER TABLE solicitations ADD COLUMN terms_accepted BOOLEAN;

ALTER TABLE solicitations
    ADD CONSTRAINT ck_solicitations_priority
        CHECK (
            priority IS NULL
            OR priority IN ('LOW', 'MEDIUM', 'HIGH')
        );
