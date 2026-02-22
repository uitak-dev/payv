-- Ledger BC schema (PostgreSQL)

create table if not exists transaction (
    transaction_id          varchar(64) primary key,
    owner_user_id           varchar(64) not null,
    transaction_type        varchar(20) not null,
    amount                  bigint not null,
    transaction_date        date not null,
    asset_id                varchar(64) not null,
    memo                    text,
    category_id_level1      varchar(64) not null,
    category_id_level2      varchar(64),
    source_type             varchar(30) not null,
    fixed_cost_template_id  varchar(64),
    constraint chk_transaction_type
        check (transaction_type in ('INCOME', 'EXPENSE')),
    constraint chk_transaction_source
        check (source_type in ('MANUAL', 'FIXED_COST_AUTO'))
);

create index if not exists idx_transaction_owner_date
    on transaction (owner_user_id, transaction_date desc);

create index if not exists idx_transaction_asset
    on transaction (asset_id);

create table if not exists transfer (
    transfer_id      varchar(64) primary key,
    owner_user_id    varchar(64) not null,
    from_asset_id    varchar(64) not null,
    to_asset_id      varchar(64) not null,
    amount           bigint not null,
    transfer_date    date not null,
    memo             text,
    created_at       timestamptz not null default now(),
    updated_at       timestamptz not null default now(),
    constraint chk_transfer_asset_pair
        check (from_asset_id <> to_asset_id),
    constraint chk_transfer_amount
        check (amount > 0)
);

create index if not exists idx_transfer_owner_date
    on transfer (owner_user_id, transfer_date desc);

create index if not exists idx_transfer_from_asset
    on transfer (from_asset_id);

create index if not exists idx_transfer_to_asset
    on transfer (to_asset_id);

create table if not exists transaction_tag (
    transaction_id  varchar(64) not null,
    tag_id          varchar(64) not null,
    owner_user_id   varchar(64),
    primary key (transaction_id, tag_id)
);

create index if not exists idx_transaction_tag_transaction
    on transaction_tag (transaction_id);

create index if not exists idx_transaction_tag_owner
    on transaction_tag (owner_user_id);

create table if not exists attachment (
    attachment_id     varchar(64) primary key,
    transaction_id    varchar(64) not null,
    owner_user_id     varchar(64) not null,
    status            varchar(20) not null,
    upload_file_name  varchar(255) not null,
    stored_file_name  varchar(255) not null,
    storage_path      varchar(512) not null,
    staging_path      varchar(512) not null,
    staging_file_name varchar(255) not null,
    content_type      varchar(100) not null,
    size_bytes        bigint not null,
    failure_reason    varchar(4000),
    constraint chk_attachment_status
        check (status in ('UPLOADING', 'STORED', 'FAILED'))
);

create index if not exists idx_attachment_tx
    on attachment (transaction_id);

create index if not exists idx_attachment_owner
    on attachment (owner_user_id);
