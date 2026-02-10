-- Budget BC schema (PostgreSQL)

create table if not exists budget (
    budget_id      varchar(64) primary key,
    owner_user_id  varchar(64) not null,
    target_month   char(7) not null,
    amount_limit   bigint not null,
    category_id    varchar(64),
    memo           text,
    is_active      boolean not null default true,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz not null default now(),
    constraint chk_budget_amount_positive check (amount_limit > 0),
    constraint chk_budget_month_format check (target_month ~ '^[0-9]{4}-[0-9]{2}$')
);

-- 1인 1유형(전체/카테고리) 1월 1예산
create unique index if not exists uq_budget_owner_month_category_active
    on budget (owner_user_id, target_month, coalesce(category_id, '__ALL__'))
    where is_active = true;

create index if not exists idx_budget_owner_month
    on budget (owner_user_id, target_month);

create index if not exists idx_budget_owner_active
    on budget (owner_user_id, is_active);

create index if not exists idx_budget_category
    on budget (category_id);
