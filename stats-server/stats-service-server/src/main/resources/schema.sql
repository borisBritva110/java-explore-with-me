CREATE TABLE IF NOT EXISTS hits
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app       VARCHAR(255) NOT NULL,
    ip        VARCHAR(46) NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE
    );

CREATE INDEX IF NOT EXISTS idx_endpoint_hit_timestamp ON hits(timestamp);
CREATE INDEX IF NOT EXISTS idx_endpoint_hit_uri ON hits(uri);