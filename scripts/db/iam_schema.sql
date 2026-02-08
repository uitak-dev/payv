-- IAM BC schema (PostgreSQL)

create table if not exists users (
    user_id        varchar(64) primary key,
    email          varchar(200) not null,
    password_hash  varchar(200) not null,
    display_name   varchar(50) not null default '',
    is_active      boolean not null default true,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz not null default now()
);

create unique index if not exists uq_users_email_active
    on users (email)
    where is_active = true;
