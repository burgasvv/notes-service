--liquibase formatted sql

--changeset burgasvv:1
create table if not exists image
(
    id           uuid default gen_random_uuid() unique not null,
    name         varchar                               not null,
    content_type varchar                               not null,
    data         bytea                                 not null
)