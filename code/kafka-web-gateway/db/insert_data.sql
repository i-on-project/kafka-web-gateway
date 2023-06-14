INSERT INTO client (client_id)
VALUES ('alex');

INSERT INTO role (name, description)
VALUES ('role1', 'Role 1 Description');

INSERT INTO client_role (client_id, role_id)
VALUES ('alex', 1);

INSERT INTO permission (topic, key, read, write)
VALUES ('topic1', 'key1', true, true);

INSERT INTO role_permission (role_id, permission_id)
VALUES (1, 1);

INSERT INTO client_permission (client_id, permission_id)
VALUES ('alex', 1);

INSERT INTO admin (name, description, owner, administrative, permission)
VALUES ('admin1', 'Admin 1 Description', true, true, true);

INSERT INTO admin_token (token_validation, admin_id, created_at, last_used_at)
VALUES ('token1', 1, current_timestamp, current_timestamp);
