INSERT INTO notification_preferences(user_id)
select id from users
on conflict (user_id) do nothing;

INSERT INTO notification_enabled_channels (preference_id, channel)
SELECT np.id, channel
FROM notification_preferences np
CROSS JOIN (VALUES ('EMAIL'), ('WEBSOCKET')) AS channels(channel)
ON CONFLICT (preference_id, channel) DO NOTHING;