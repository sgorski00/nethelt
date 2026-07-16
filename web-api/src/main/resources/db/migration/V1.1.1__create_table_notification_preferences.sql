CREATE TABLE notification_preferences (
    id bigserial primary key,
    user_id bigint NOT NULL UNIQUE references users(id) ON DELETE CASCADE
);

CREATE TABLE notification_enabled_channels (
    preference_id bigint NOT NULL references notification_preferences(id) ON DELETE CASCADE,
    channel varchar(255) not null,
    CONSTRAINT pk_notification_enabled_channels PRIMARY KEY (user_id, channel)
);