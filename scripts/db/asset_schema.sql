-- Asset BC schema (PostgreSQL)

create table if not exists asset (
    asset_id       varchar(64) primary key,
    owner_user_id  varchar(64) not null,
    name           varchar(20) not null,
    asset_type     varchar(30) not null,
    is_active      boolean not null default true,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz not null default now(),
    constraint chk_asset_type
        check (asset_type in ('CARD', 'CASH', 'BANK_ACCOUNT'))
);

create unique index if not exists uq_asset_owner_name_active
    on asset (owner_user_id, name)
    where is_active = true;

create index if not exists idx_asset_owner
    on asset (owner_user_id);
