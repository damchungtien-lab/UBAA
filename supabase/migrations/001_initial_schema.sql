-- 自动预约任务表
CREATE TABLE IF NOT EXISTS auto_booking_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,
    course_id BIGINT NOT NULL,
    course_name TEXT NOT NULL,
    course_position TEXT,
    course_teacher TEXT,
    course_select_start_date TIMESTAMPTZ,
    course_select_end_date TIMESTAMPTZ,
    access_token TEXT NOT NULL,       -- JWT access token for the BYKC API
    refresh_token TEXT NOT NULL,       -- JWT refresh token for renewal
    created_at TIMESTAMPTZ DEFAULT now(),
    is_completed BOOLEAN DEFAULT false,
    is_failed BOOLEAN DEFAULT false,
    result_message TEXT,
    executed_at TIMESTAMPTZ
);

-- 索引加速查询
CREATE INDEX IF NOT EXISTS idx_auto_booking_pending ON auto_booking_tasks (is_completed, course_select_start_date);
CREATE INDEX IF NOT EXISTS idx_auto_booking_user ON auto_booking_tasks (user_id);

-- 密码保管箱表
CREATE TABLE IF NOT EXISTS vault_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,
    title TEXT NOT NULL,
    system_name TEXT DEFAULT '',
    username TEXT DEFAULT '',
    password_encrypted TEXT NOT NULL,
    url TEXT DEFAULT '',
    notes_encrypted TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_vault_entries_user ON vault_entries (user_id);

-- 保管箱主密码哈希
CREATE TABLE IF NOT EXISTS vault_master (
    user_id TEXT PRIMARY KEY,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- RLS 策略
ALTER TABLE auto_booking_tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE vault_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE vault_master ENABLE ROW LEVEL SECURITY;

-- 用户只能访问自己的数据 (通过 anon key 的 JWT 中的 sub 匹配)
CREATE POLICY "Users can access own tasks" ON auto_booking_tasks
    FOR ALL USING (user_id = auth.uid());

CREATE POLICY "Users can access own vault" ON vault_entries
    FOR ALL USING (user_id = auth.uid());

CREATE POLICY "Users can access own vault master" ON vault_master
    FOR ALL USING (user_id = auth.uid());

-- 启用 pg_cron 扩展（需要在 Supabase 仪表盘手动启用）
-- CREATE EXTENSION IF NOT EXISTS pg_cron;
