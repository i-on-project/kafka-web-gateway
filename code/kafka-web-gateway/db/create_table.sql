CREATE TABLE IF NOT EXISTS User_Info
(
    user_id  BIGINT PRIMARY KEY,
    username VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS Session
(
    session_id    BIGINT PRIMARY KEY,
    user_id       BIGINT,
    created_at    TIMESTAMP,
    gateway_topic VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES User_Info (user_id)
);

CREATE TABLE IF NOT EXISTS Active_Session
(
    session_id BIGINT PRIMARY KEY NOT NULL,
    updated_at TIMESTAMP          NOT NULL,
    FOREIGN KEY (session_id) REFERENCES Session (session_id)
);

CREATE TABLE IF NOT EXISTS Subscription
(
    session_id BIGINT       NOT NULL,
    topic      VARCHAR(255) NOT NULL,
    key        VARCHAR(255),
    FOREIGN KEY (session_id) REFERENCES Session (session_id)
);

CREATE TABLE IF NOT EXISTS Permission
(
    user_id BIGINT       NOT NULL,
    topic   VARCHAR(255) NOT NULL,
    key     VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES User_Info (user_id)
);
