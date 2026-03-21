--liquibase formatted sql

--changeset burgasvv:1
create table if not exists note
(
    id          uuid default gen_random_uuid() unique not null,
    title       varchar                               not null,
    content     text                                  not null,
    created_at  timestamp                             not null,
    identity_id uuid references identity (id) on delete cascade on update cascade
)