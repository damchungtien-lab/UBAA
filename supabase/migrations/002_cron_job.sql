-- 定时触发自动选课检查（每分钟）
-- 注意：需要在 Supabase 仪表盘中手动启用 pg_cron 扩展
-- 然后执行本迁移以创建定时任务

-- 查询待执行的自动选课任务
SELECT cron.schedule(
    'auto-book-check',
    '* * * * *',   -- 每分钟执行
    $$
    SELECT
      net.http_post(
        url := (SELECT current_setting('app.settings.edge_function_url') || '/auto-book'),
        headers := '{"Content-Type": "application/json", "Authorization": "Bearer ' || current_setting('app.settings.service_role_key') || '"}'::jsonb,
        body := '{}'::jsonb
      );
    $$
);
