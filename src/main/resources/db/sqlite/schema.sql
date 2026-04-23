CREATE TABLE IF NOT EXISTS terminal
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    terminal_code  TEXT    NOT NULL,
    terminal_name  TEXT,
    secret_hash    TEXT    NOT NULL,
    status         INTEGER NOT NULL DEFAULT 0,
    last_login_at  TEXT,
    created_at     TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted        INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_terminal_code
    ON terminal (terminal_code, deleted);

CREATE TABLE IF NOT EXISTS patient
(
    id                       INTEGER PRIMARY KEY AUTOINCREMENT,
    rfid_uuid                TEXT,
    id_card_no               TEXT NOT NULL,
    insurance_no             TEXT,
    phone                    TEXT,
    name                     TEXT NOT NULL,
    gender                   INTEGER,
    age                      INTEGER,
    medical_history          TEXT,
    emergency_contact_name   TEXT,
    emergency_contact_phone  TEXT,
    created_at               TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                  INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_rfid_active
    ON patient (rfid_uuid, deleted);

CREATE UNIQUE INDEX IF NOT EXISTS uk_id_card
    ON patient (id_card_no, deleted);

CREATE INDEX IF NOT EXISTS idx_phone
    ON patient (phone);
