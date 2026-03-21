--liquibase formatted sql

--changeset burgasvv:1
create table if not exists note_image
(
    note_id  uuid references note (id) on delete cascade on update cascade,
    image_id uuid references image (id) on delete cascade on update cascade,
    primary key (note_id, image_id)
)