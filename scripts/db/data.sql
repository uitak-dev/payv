-- payv seed data (PostgreSQL)

begin;

-- 1) Account (owner)
-- email: user01@gmail.com
-- password: 1111
insert into users (
    user_id,
    email,
    password_hash,
    display_name,
    is_active,
    created_at,
    updated_at
) values (
    'usr_demo_001',
    'user01@gmail.com',
    '$2a$10$FLaQLL5EFgW7Zk1aDoFg/.TpO9hwlFzO8n8eoMGzMDnXcd4.mZBaa',
    'Payv Demo',
    true,
    now(),
    now()
)
on conflict (user_id) do update set
    email = excluded.email,
    password_hash = excluded.password_hash,
    display_name = excluded.display_name,
    is_active = excluded.is_active,
    updated_at = now();

-- 2) Categories (3 roots + 9 children)
insert into category (category_id, owner_user_id, name, parent_id, depth, is_system, is_active, created_at, updated_at) values
    ('cat_r_food', 'usr_demo_001', '식비', null, 1, false, true, now(), now()),
    ('cat_r_transport', 'usr_demo_001', '교통', null, 1, false, true, now(), now()),
    ('cat_r_life', 'usr_demo_001', '생활/여가', null, 1, false, true, now(), now()),
    ('cat_c_food_grocery', 'usr_demo_001', '장보기', 'cat_r_food', 2, false, true, now(), now()),
    ('cat_c_food_coffee', 'usr_demo_001', '카페/커피', 'cat_r_food', 2, false, true, now(), now()),
    ('cat_c_food_dining', 'usr_demo_001', '외식', 'cat_r_food', 2, false, true, now(), now()),
    ('cat_c_trans_subway', 'usr_demo_001', '대중교통', 'cat_r_transport', 2, false, true, now(), now()),
    ('cat_c_trans_taxi', 'usr_demo_001', '택시', 'cat_r_transport', 2, false, true, now(), now()),
    ('cat_c_trans_fuel', 'usr_demo_001', '주유', 'cat_r_transport', 2, false, true, now(), now()),
    ('cat_c_life_movie', 'usr_demo_001', '영화/공연', 'cat_r_life', 2, false, true, now(), now()),
    ('cat_c_life_shopping', 'usr_demo_001', '쇼핑', 'cat_r_life', 2, false, true, now(), now()),
    ('cat_c_life_hobby', 'usr_demo_001', '취미', 'cat_r_life', 2, false, true, now(), now())
on conflict (category_id) do update set
    owner_user_id = excluded.owner_user_id,
    name = excluded.name,
    parent_id = excluded.parent_id,
    depth = excluded.depth,
    is_system = excluded.is_system,
    is_active = excluded.is_active,
    updated_at = now();

-- 3) Tags (4)
insert into tag (tag_id, owner_user_id, name, is_active, created_at, updated_at) values
    ('tag_fixed', 'usr_demo_001', '고정비', true, now(), now()),
    ('tag_emergency', 'usr_demo_001', '비상지출', true, now(), now()),
    ('tag_date', 'usr_demo_001', '데이트', true, now(), now()),
    ('tag_work', 'usr_demo_001', '업무', true, now(), now())
on conflict (tag_id) do update set
    owner_user_id = excluded.owner_user_id,
    name = excluded.name,
    is_active = excluded.is_active,
    updated_at = now();

-- 4) Assets (3)
insert into asset (asset_id, owner_user_id, name, asset_type, is_active, created_at, updated_at) values
    ('ast_card_main', 'usr_demo_001', '메인카드', 'CARD', true, now(), now()),
    ('ast_cash_wallet', 'usr_demo_001', '지갑현금', 'CASH', true, now(), now()),
    ('ast_bank_checking', 'usr_demo_001', '입출금통장', 'BANK_ACCOUNT', true, now(), now())
on conflict (asset_id) do update set
    owner_user_id = excluded.owner_user_id,
    name = excluded.name,
    asset_type = excluded.asset_type,
    is_active = excluded.is_active,
    updated_at = now();

-- 5) Budgets (overall + per root category)
insert into budget (
    budget_id,
    owner_user_id,
    target_month,
    amount_limit,
    category_id,
    memo,
    is_active,
    created_at,
    updated_at
) values
    ('bud_overall_2026_02', 'usr_demo_001', '2026-02', 2500000, null, '2월 전체 예산', true, now(), now()),
    ('bud_food_2026_02', 'usr_demo_001', '2026-02', 800000, 'cat_r_food', '식비 예산', true, now(), now()),
    ('bud_transport_2026_02', 'usr_demo_001', '2026-02', 400000, 'cat_r_transport', '교통 예산', true, now(), now()),
    ('bud_life_2026_02', 'usr_demo_001', '2026-02', 500000, 'cat_r_life', '생활/여가 예산', true, now(), now())
on conflict (budget_id) do update set
    owner_user_id = excluded.owner_user_id,
    target_month = excluded.target_month,
    amount_limit = excluded.amount_limit,
    category_id = excluded.category_id,
    memo = excluded.memo,
    is_active = excluded.is_active,
    updated_at = now();

-- 6) Transactions (4, no attachments)
insert into transaction (
    transaction_id,
    owner_user_id,
    transaction_type,
    amount,
    transaction_date,
    asset_id,
    memo,
    category_id_level1,
    category_id_level2,
    source_type,
    fixed_cost_template_id
) values
    ('tx_20260201_001', 'usr_demo_001', 'EXPENSE', 18000, date '2026-02-01', 'ast_card_main', '점심 식사', 'cat_r_food', 'cat_c_food_dining', 'MANUAL', null),
    ('tx_20260203_001', 'usr_demo_001', 'EXPENSE', 4500, date '2026-02-03', 'ast_cash_wallet', '커피', 'cat_r_food', 'cat_c_food_coffee', 'MANUAL', null),
    ('tx_20260205_001', 'usr_demo_001', 'EXPENSE', 52000, date '2026-02-05', 'ast_bank_checking', '택시 이동', 'cat_r_transport', 'cat_c_trans_taxi', 'MANUAL', null),
    ('tx_20260208_001', 'usr_demo_001', 'EXPENSE', 37000, date '2026-02-08', 'ast_card_main', '영화 관람', 'cat_r_life', 'cat_c_life_movie', 'MANUAL', null)
on conflict (transaction_id) do update set
    owner_user_id = excluded.owner_user_id,
    transaction_type = excluded.transaction_type,
    amount = excluded.amount,
    transaction_date = excluded.transaction_date,
    asset_id = excluded.asset_id,
    memo = excluded.memo,
    category_id_level1 = excluded.category_id_level1,
    category_id_level2 = excluded.category_id_level2,
    source_type = excluded.source_type,
    fixed_cost_template_id = excluded.fixed_cost_template_id;

-- Optional transaction-tag mappings for richer sample data
insert into transaction_tag (transaction_id, tag_id, owner_user_id) values
    ('tx_20260201_001', 'tag_fixed', 'usr_demo_001'),
    ('tx_20260203_001', 'tag_date', 'usr_demo_001'),
    ('tx_20260205_001', 'tag_work', 'usr_demo_001'),
    ('tx_20260208_001', 'tag_date', 'usr_demo_001')
on conflict (transaction_id, tag_id) do nothing;

commit;
