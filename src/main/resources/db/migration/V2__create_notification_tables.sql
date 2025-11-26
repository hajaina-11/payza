CREATE TABLE pz_notification (
                                 id UUID PRIMARY KEY,
                                 user_id UUID NOT NULL,
                                 type VARCHAR(50) NOT NULL,
                                 channel VARCHAR(20) NOT NULL,
                                 title VARCHAR(200) NOT NULL,
                                 body TEXT NOT NULL,
                                 template VARCHAR(100),
                                 status VARCHAR(20) NOT NULL,
                                 metadata JSONB,
                                 created_at TIMESTAMP NOT NULL,
                                 updated_at TIMESTAMP,
                                 sent_at TIMESTAMP,
                                 delivered_at TIMESTAMP
);

CREATE INDEX idx_notification_user_id ON pz_notification(user_id);
CREATE INDEX idx_notification_status ON pz_notification(status);
CREATE INDEX idx_notification_created ON pz_notification(created_at);