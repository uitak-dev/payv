-- Notification BC schema (PostgreSQL)

create table if not exists notification (
    notification_id     varchar(64) primary key,
    owner_user_id       varchar(64) not null,
    notification_type   varchar(50) not null,
    title               varchar(200) not null,
    message             text not null,
    reference_type      varchar(40),
    reference_id        varchar(64),
    is_read             boolean not null default false,
    read_at             timestamptz,
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now(),
    constraint chk_notification_type
        check (notification_type in (
            'BUDGET_THRESHOLD_50',
            'BUDGET_THRESHOLD_100',
            'FIXED_EXPENSE_AUTO_CREATED'
        ))
);

create index if not exists idx_notification_owner_created
    on notification (owner_user_id, created_at desc);

create index if not exists idx_notification_owner_unread
    on notification (owner_user_id, is_read);

create table if not exists notification_dispatch_log (
    dispatch_key        varchar(200) primary key,
    owner_user_id       varchar(64) not null,
    notification_type   varchar(50) not null,
    created_at          timestamptz not null default now(),
    constraint chk_notification_dispatch_type
        check (notification_type in (
            'BUDGET_THRESHOLD_50',
            'BUDGET_THRESHOLD_100',
            'FIXED_EXPENSE_AUTO_CREATED'
        ))
);

create index if not exists idx_notification_dispatch_owner
    on notification_dispatch_log (owner_user_id, created_at desc);
