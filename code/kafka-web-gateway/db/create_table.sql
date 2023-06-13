create table if not exists client
(
    client_id bigint primary key
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
    created_at timestamp default now(),
    updated_at timestamp not null,
    active     boolean   not null
);

create table if not exists subscription
(
    subscription_id int generated always as identity primary key,
    session_id      bigint       not null references session (session_id) on delete cascade on update cascade,
    topic           varchar(255) not null,
    key             varchar(255),
    unique (session_id, topic, key)
);

create table if not exists role
(
    role_id     int generated always as identity primary key,
    name        varchar(36) unique not null,
    description varchar(255)
);

create table if not exists client_role
(
    client_id bigint references client (client_id) on delete cascade on update cascade,
    role_id   int references role (role_id) on delete cascade on update cascade,
    primary key (client_id, role_id)
);

create table if not exists permission
(
    permission_id int generated always as identity primary key,
    topic         varchar(255) not null,
    key           varchar(255),
    read          boolean      NOT NULL,
    write         boolean      NOT NULL,
    unique (topic, key, read, write)
);

create table if not exists role_permission
(
    role_id       int references role (role_id) on delete cascade on update cascade,
    permission_id int references permission (permission_id) on delete cascade on update cascade,
    primary key (role_id, permission_id)
);

create table if not exists client_permission
(
    client_id     bigint references client (client_id) on delete cascade on update cascade,
    permission_id int references permission (permission_id) on delete cascade on update cascade,
    primary key (client_id, permission_id)
);

create table if not exists admin
(
    admin_id       int generated always as identity primary key,
    name           varchar(64) not null,
    description    varchar(255),
    owner          boolean     not null,
    administrative boolean     not null,
    permission     boolean     not null
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
    name        varchar(64) primary key,
    value       varchar(255) not null,
    description varchar(255),
    updated_at  timestamp    not null
);
