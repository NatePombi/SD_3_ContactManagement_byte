create table contacts(
    id BIGINT primary key,
    full_name varchar(120) not null,
    email varchar(254) not null ,
    phone_number varchar(30) not null unique
);

create unique INDEX ux_contacts_email_lower
on contacts (LOWER(email))