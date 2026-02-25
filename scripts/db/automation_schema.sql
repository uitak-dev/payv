-- Automation BC schema (PostgreSQL)
-- 목적:
-- 1) fixed_expense_definition: 사용자가 등록한 고정비 "규칙(마스터)"
-- 2) fixed_expense_execution: 특정 날짜에 실제로 처리할/처리한 "실행 인스턴스"
-- 참고:
-- - Spring Batch 메타 테이블은 scripts/db/spring_batch_schema.sql 을 별도 적용한다.

create table if not exists fixed_expense_definition (
    definition_id         varchar(64) primary key,
    owner_user_id         varchar(64) not null,
    name                  varchar(120) not null,
    amount                bigint not null,
    asset_id              varchar(64) not null,
    category_id_level1    varchar(64) not null,
    category_id_level2    varchar(64),
    memo                  text,
    cycle                 varchar(20) not null,
    day_of_month          smallint,
    is_end_of_month       boolean not null default false,
    is_active             boolean not null default true,
    created_at            timestamptz not null default now(),
    updated_at            timestamptz not null default now(),
    -- 현재 정책: 월 단위만 허용
    constraint chk_fixed_expense_definition_cycle
        check (cycle in ('MONTHLY')),
    constraint chk_fixed_expense_definition_amount
        check (amount > 0),
    -- 스케줄 정책:
    -- - 말일 모드면 day_of_month는 null
    -- - 일반 모드면 day_of_month는 1~31
    constraint chk_fixed_expense_definition_schedule
        check (
            (is_end_of_month = true and day_of_month is null)
            or
            (is_end_of_month = false and day_of_month between 1 and 31)
        )
);

-- 사용자별 활성 마스터 조회 최적화
create index if not exists idx_fixed_expense_definition_owner_active
    on fixed_expense_definition (owner_user_id, is_active);

-- runDate 기준 실행 대상(active/eom/day_of_month) 필터링 최적화
create index if not exists idx_fixed_expense_definition_schedule
    on fixed_expense_definition (is_active, is_end_of_month, day_of_month);

create table if not exists fixed_expense_execution (
    execution_id            varchar(64) primary key,
    definition_id           varchar(64) not null,
    owner_user_id           varchar(64) not null,
    definition_name         varchar(120) not null,
    amount                  bigint not null,
    asset_id                varchar(64) not null,
    category_id_level1      varchar(64) not null,
    category_id_level2      varchar(64),
    memo                    text,
    scheduled_date          date not null,
    status                  varchar(20) not null,
    transaction_id          varchar(64),
    failure_reason          varchar(1000),
    batch_job_execution_id  bigint,
    processed_at            timestamp,
    created_at              timestamptz not null default now(),
    updated_at              timestamptz not null default now(),
    -- 실행 인스턴스는 반드시 기존 definition을 참조
    constraint fk_fixed_expense_execution_definition
        foreign key (definition_id) references fixed_expense_definition (definition_id),
    constraint chk_fixed_expense_execution_status
        check (status in ('PLANNED', 'SUCCEEDED', 'FAILED', 'SKIPPED')),
    constraint chk_fixed_expense_execution_amount
        check (amount > 0)
);

-- 배치 재실행 시 같은 날짜/마스터 실행 건 중복 생성 방지
create unique index if not exists uq_fixed_expense_execution_dedup
    on fixed_expense_execution (owner_user_id, definition_id, scheduled_date);

-- 특정일 PLANNED 조회 최적화
create index if not exists idx_fixed_expense_execution_planned
    on fixed_expense_execution (status, scheduled_date);

create index if not exists idx_fixed_expense_execution_owner
    on fixed_expense_execution (owner_user_id);
