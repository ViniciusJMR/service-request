CREATE TABLE solicitations (
    id UUID NOT NULL PRIMARY KEY,
    client_id UUID NOT NULL,
    status VARCHAR(30),
    current_step INTEGER,
    type VARCHAR(30),
    title VARCHAR(80),
    description VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    submitted_at TIMESTAMP WITH TIME ZONE,
    analyzed_at TIMESTAMP WITH TIME ZONE,
    analyzed_by UUID,
    analyzis_comment VARCHAR(1000),

    CONSTRAINT fk_solicitations_client
        FOREIGN KEY (client_id)
        REFERENCES users (id),

    CONSTRAINT fk_solicitations_analyzed_by
        FOREIGN KEY (analyzed_by)
        REFERENCES users (id),

    CONSTRAINT ck_solicitations_status
        CHECK (
            status IS NULL
            OR status IN ('DRAFT', 'SUBMITTED', 'IN_REVIEW', 'APPROVED', 'REJECTED')
        ),

    CONSTRAINT ck_solicitations_type
        CHECK (
            type IS NULL
            OR type IN ('INSTALLATION', 'MAINTENANCE', 'INSPECTION')
        )
);

CREATE INDEX idx_solicitations_client_id
    ON solicitations (client_id);

CREATE INDEX idx_solicitations_analyzed_by
    ON solicitations (analyzed_by);
