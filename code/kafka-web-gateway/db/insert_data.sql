INSERT INTO client (client_id, username)
VALUES (1, 'user1'),
       (2, 'user2'),
       (3, 'user3');

INSERT INTO gateway (gateway_id, topic_clients, topic_commands, active, updated_at)
VALUES (1, 'topic1', 'command1', true, current_timestamp),
       (2, 'topic2', 'command2', true, current_timestamp),
       (3, 'topic3', 'command3', false, current_timestamp);

INSERT INTO session (session_id, client_id, gateway_id, created_at)
VALUES (1, 1, 1, current_timestamp),
       (2, 2, 2, current_timestamp),
       (3, 3, 3, current_timestamp);

INSERT INTO active_session (session_id, updated_at)
VALUES (1, current_timestamp),
       (2, current_timestamp),
       (3, current_timestamp);

INSERT INTO subscription (session_id, topic, key)
VALUES (1, 'topic1', 'key1'),
       (2, 'topic2', 'key2'),
       (3, 'topic3', 'key3');

INSERT INTO client_role (role_id, role, description)
VALUES (1, 'role1', 'Role 1 Description'),
       (2, 'role2', 'Role 2 Description'),
       (3, 'role3', 'Role 3 Description');

INSERT INTO client_role_assignment (client_id, role_id)
VALUES (1, 1),
       (2, 2),
       (3, 3);

INSERT INTO client_permission (permission_id, topic, key, read, write)
VALUES (1, 'topic1', 'key1', true, true),
       (2, 'topic2', 'key2', true, false),
       (3, 'topic3', 'key3', false, true);

INSERT INTO client_role_permission (role_id, permission_id)
VALUES (1, 1),
       (2, 2),
       (3, 3);

INSERT INTO admin (admin_id, username, password_validation, description, owner)
VALUES (1, 'admin1', 'password1', 'Admin 1 Description', true),
       (2, 'admin2', 'password2', 'Admin 2 Description', false),
       (3, 'admin3', 'password3', 'Admin 3 Description', false);

INSERT INTO admin_role (role_id, role, description)
VALUES (1, 'role1', 'Role 1 Description'),
       (2, 'role2', 'Role 2 Description'),
       (3, 'role3', 'Role 3 Description');

INSERT INTO admin_role_assignment (admin_id, role_id)
VALUES (1, 1),
       (2, 2),
       (3, 3);

INSERT INTO admin_permission (permission_id, administrative, client_permission)
VALUES (1, true, true),
       (2, false, true),
       (3, true, false);

INSERT INTO admin_role_permission (role_id, permission_id)
VALUES (1, 1),
       (2, 2),
       (3, 3);

INSERT INTO admin_token (token_validation, admin_id, created_at, last_used_at)
VALUES ('token1', 1, current_timestamp, current_timestamp),
       ('token2', 2, current_timestamp, current_timestamp),
       ('token3', 3, current_timestamp, current_timestamp);

INSERT INTO setting (setting_name, setting_value, setting_description, updated_at)
VALUES ('setting1', 'value1', 'Setting 1 Description', current_timestamp),
       ('setting2', 'value2', 'Setting 2 Description', current_timestamp),
       ('setting3', 'value3', 'Setting 3 Description', current_timestamp);