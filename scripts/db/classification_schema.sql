-- Classification BC schema (PostgreSQL)

create table if not exists category (
    category_id    varchar(64) primary key,
    owner_user_id  varchar(64),
    name           varchar(200) not null,
    parent_id      varchar(64),
    depth          integer not null,
    is_system      boolean not null default false,
    is_active      boolean not null default true,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz not null default now(),
    constraint chk_category_depth check (depth in (1, 2)),
    constraint chk_category_parent
        check (
            (depth = 1 and parent_id is null) or
            (depth = 2 and parent_id is not null)
        )
);

create index if not exists idx_category_owner_depth
    on category (owner_user_id, depth);

create index if not exists idx_category_parent
    on category (parent_id);

create table if not exists tag (
    tag_id         varchar(64) primary key,
    owner_user_id  varchar(64) not null,
    name           varchar(200) not null,
    is_active      boolean not null default true,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz not null default now()
);

create index if not exists idx_tag_owner
    on tag (owner_user_id);
