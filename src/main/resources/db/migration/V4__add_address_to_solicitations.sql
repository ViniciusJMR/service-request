ALTER TABLE solicitations ADD COLUMN cep VARCHAR(8);
ALTER TABLE solicitations ADD COLUMN number VARCHAR(20);
ALTER TABLE solicitations ADD COLUMN complement VARCHAR(100);
ALTER TABLE solicitations ADD COLUMN street VARCHAR(120);
ALTER TABLE solicitations ADD COLUMN neighborhood VARCHAR(80);
ALTER TABLE solicitations ADD COLUMN city VARCHAR(80);
ALTER TABLE solicitations ADD COLUMN state_id INTEGER;

ALTER TABLE solicitations
    ADD CONSTRAINT fk_solicitations_state
        FOREIGN KEY (state_id)
        REFERENCES states (id);
