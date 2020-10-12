create table if not exists users (
    id varchar(64) not null primary key unique,
    email varchar(64) not null unique,
    password varchar(64) not null,
    username varchar(64) unique
);

create table if not exists roles (
    id varchar(64) not null primary key unique,
    name varchar(64) not null unique
);

create table if not exists users_roles (
    roles_id varchar(64) not null unique,
    users_id varchar(64) not null unique,
    foreign key (roles_id)
          references roles (id),
    foreign key (users_id)
          references users (id)
);