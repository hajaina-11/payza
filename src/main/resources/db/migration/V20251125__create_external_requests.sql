CREATE TABLE IF NOT EXISTS external_requests (
                                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
    status VARCHAR(50),
    external_reference VARCHAR(255)
    );
