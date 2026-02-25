-- Spring Batch metadata schema (PostgreSQL)
-- 적용 목적:
-- - Job/Step 실행 이력, 파라미터, 실행 컨텍스트 저장
-- - 재시작/중복 실행 방지/모니터링을 위한 내부 메타데이터 유지

create sequence if not exists batch_job_seq
    minvalue 0
    start with 0
    increment by 1
    no maxvalue
    cache 1;

create sequence if not exists batch_job_execution_seq
    minvalue 0
    start with 0
    increment by 1
    no maxvalue
    cache 1;

create sequence if not exists batch_step_execution_seq
    minvalue 0
    start with 0
    increment by 1
    no maxvalue
    cache 1;

create table if not exists batch_job_instance (
    job_instance_id bigint primary key,
    version bigint,
    job_name varchar(100) not null,
    job_key varchar(32) not null,
    constraint batch_job_inst_un unique (job_name, job_key)
);

create table if not exists batch_job_execution (
    job_execution_id bigint primary key,
    version bigint,
    job_instance_id bigint not null,
    create_time timestamp not null,
    job_configuration_location varchar(2500),
    start_time timestamp,
    end_time timestamp,
    status varchar(10),
    exit_code varchar(2500),
    exit_message varchar(2500),
    last_updated timestamp,
    constraint batch_job_exec_inst_fk
        foreign key (job_instance_id)
            references batch_job_instance (job_instance_id)
);

create table if not exists batch_job_execution_params (
    job_execution_id bigint not null,
    type_cd varchar(6) not null,
    key_name varchar(100) not null,
    string_val varchar(250),
    date_val timestamp,
    long_val bigint,
    double_val double precision,
    identifying char(1) not null,
    constraint batch_job_exec_params_fk
        foreign key (job_execution_id)
            references batch_job_execution (job_execution_id)
);

create table if not exists batch_step_execution (
    step_execution_id bigint primary key,
    version bigint not null,
    step_name varchar(100) not null,
    job_execution_id bigint not null,
    start_time timestamp not null,
    end_time timestamp,
    status varchar(10),
    commit_count bigint,
    read_count bigint,
    filter_count bigint,
    write_count bigint,
    read_skip_count bigint,
    write_skip_count bigint,
    process_skip_count bigint,
    rollback_count bigint,
    exit_code varchar(2500),
    exit_message varchar(2500),
    last_updated timestamp,
    constraint batch_step_exec_job_fk
        foreign key (job_execution_id)
            references batch_job_execution (job_execution_id)
);

create table if not exists batch_step_execution_context (
    step_execution_id bigint primary key,
    short_context varchar(2500) not null,
    serialized_context text,
    constraint batch_step_exec_ctx_fk
        foreign key (step_execution_id)
            references batch_step_execution (step_execution_id)
);

create table if not exists batch_job_execution_context (
    job_execution_id bigint primary key,
    short_context varchar(2500) not null,
    serialized_context text,
    constraint batch_job_exec_ctx_fk
        foreign key (job_execution_id)
            references batch_job_execution (job_execution_id)
);

create index if not exists batch_job_inst_exec_fk_idx
    on batch_job_execution (job_instance_id);

create index if not exists batch_job_exec_step_fk_idx
    on batch_step_execution (job_execution_id);

create index if not exists batch_job_exec_params_fk_idx
    on batch_job_execution_params (job_execution_id);

-- Spring Batch 4.3+ 조회 쿼리에서 필요.
-- 기존 스키마(컬럼 없는 버전)에서 점진적으로 적용될 수 있도록 보정한다.
alter table if exists batch_job_execution
    add column if not exists job_configuration_location varchar(2500);
