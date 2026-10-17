create type relationship_status as enum('MANAGES','MENTORS','KNOWS');

create table contact_relationships(
    id BIGSERIAL primary key ,
    source_contact_id BIGINT not null references contacts(id) on delete cascade,
    target_contact_id BIGINT not null references contacts(id) on delete cascade,
    relationship_type relationship_status not null ,

    constraint chk_no_self_relationship check ( source_contact_id <> target_contact_id),

     constraint uk_contact_relationship unique (source_contact_id,target_contact_id,relationship_type)

);

create index idx_relationship_target on contact_relationships(target_contact_id);