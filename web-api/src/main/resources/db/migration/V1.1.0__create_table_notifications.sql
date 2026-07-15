CREATE TABLE notifications (
    id bigserial PRIMARY KEY,
    user_id bigint not null unique references users(id),
    title varchar(255) NOT NULL,
    content varchar(255) NOT NULL,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);