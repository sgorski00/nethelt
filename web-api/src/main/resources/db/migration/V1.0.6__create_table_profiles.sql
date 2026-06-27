CREATE TABLE profiles (
    id bigserial PRIMARY KEY,
    user_id bigint not null unique references users(id),
    username varchar(255) NOT NULL UNIQUE,
    first_name varchar(255),
    last_name varchar(255),
    birth_date date,
    bio text,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);