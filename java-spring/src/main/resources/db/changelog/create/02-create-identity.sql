--liquibase formatted sql

--changeset burgasvv:1
create table if not exists identity
(
    id         uuid                    default gen_random_uuid() unique not null,
    authority  varchar        not null,
    username   varchar unique not null,
    password   varchar        not null,
    email      varchar unique not null,
    enabled    boolean        not null default true,
    firstname  varchar        not null,
    lastname   varchar        not null,
    patronymic varchar        not null,
    image_id   uuid unique references image (id) on delete set null on update cascade
)