create table if not exists client -- user is a reserved keyword
(
    client_id bigint primary key,
    username  varchar(255),
    constraint username_check check (char_length(username) >= 4 and char_length(username) <= 36)
);

create table if not exists gateway
(
    gateway_id     bigint primary key,
    topic_clients  varchar(255) not null,
    topic_commands varchar(255) not null,
    active         boolean      not null,
    updated_at     timestamp    not null
);

create table if not exists session
(
    session_id bigint primary key,
    client_id  bigint references client (client_id) on delete cascade on update cascade,
    gateway_id bigint references gateway (gateway_id) on delete cascade on update cascade,
    created_at timestamp default now()
);

create table if not exists active_session
(
    session_id bigint primary key references session (session_id) on delete cascade on update cascade,
    updated_at timestamp not null
);

create table if not exists subscription
(
    subscription_id int generated always as identity primary key,
    session_id      bigint       not null references session (session_id) on delete cascade on update cascade,
    topic           varchar(255) not null,
    key             varchar(255),
    unique (session_id, topic, key)
);

create table if not exists client_role
(
    role_id     int generated always as identity primary key,
    role        varchar(36) unique not null,
    description varchar(255)
);

create table if not exists client_role_assignment
(
    client_id bigint references client (client_id) on delete cascade on update cascade,
    role_id   int references client_role (role_id) on delete cascade on update cascade,
    primary key (client_id, role_id)
);

create table if not exists client_permission
(
    permission_id int generated always as identity primary key,
    topic         varchar(255) not null,
    key           varchar(255),
    read          boolean      NOT NULL,
    write         boolean      NOT NULL,
    unique (permission_id, topic, key)
);

create table if not exists client_role_permission
(
    role_id       int references client_role (role_id) on delete cascade on update cascade,
    permission_id int references client_permission (permission_id) on delete cascade on update cascade,
    primary key (role_id, permission_id)
);

create table if not exists admin
(
    admin_id            int generated always as identity primary key,
    username            varchar(36) unique,
    password_validation varchar(255) not null,
    description         varchar(255),
    owner               boolean      not null,
    constraint username_check check (char_length(username) >= 4 and char_length(username) <= 36)
);

create table if not exists admin_role
(
    role_id     int generated always as identity primary key,
    role        varchar(36) unique not null,
    description varchar(255)
);

create table if not exists admin_role_assignment
(
    admin_id int references admin (admin_id) on delete cascade on update cascade,
    role_id  int references admin_role (role_id) on delete cascade on update cascade,
    primary key (admin_id, role_id)
);

create table if not exists admin_permission
(
    permission_id     int generated always as identity primary key,
    administrative    boolean not null,
    client_permission boolean not null,
    unique (permission_id, administrative, client_permission)
);

create table if not exists admin_role_permission
(
    role_id       int references admin_role (role_id) on delete cascade on update cascade,
    permission_id int references admin_permission (permission_id) on delete cascade on update cascade,
    primary key (role_id, permission_id)
);

create table if not exists admin_token
(
    token_validation varchar(255) primary key,
    admin_id         int       not null references admin (admin_id) on delete cascade on update cascade,
    created_at       timestamp not null default now(),
    last_used_at     timestamp not null
);

create table if not exists setting
(
    setting_name        varchar(64) primary key,
    setting_value       varchar(255) not null,
    setting_description varchar(255) not null,
    updated_at          timestamp    not null
);
