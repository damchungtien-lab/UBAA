/**
 * 博雅课程自动预约 Edge Function
 * 每60秒由 pg_cron 触发，检查是否有需要执行的抢课任务
 */

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
const UAAA_API_BASE = Deno.env.get("UAAA_API_BASE") || "https://ubaa.mofrp.top:2021";

Deno.serve(async (_req) => {
  const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);

  const now = new Date().toISOString();

  // 查找所有未完成且选课时间已到的任务
  const { data: tasks, error } = await supabase
    .from("auto_booking_tasks")
    .select("*")
    .eq("is_completed", false)
    .lte("course_select_start_date", now);

  if (error || !tasks || tasks.length === 0) {
    return new Response(JSON.stringify({ processed: 0 }), {
      headers: { "Content-Type": "application/json" },
    });
  }

  const results: Record<string, unknown>[] = [];

  for (const task of tasks) {
    const taskResult: Record<string, unknown> = {
      task_id: task.id,
      course_name: task.course_name,
    };

    try {
      // 先尝试刷新 token
      let accessToken = task.access_token;
      try {
        const refreshResp = await fetch(`${UAAA_API_BASE}/api/v1/auth/refresh`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${task.refresh_token}`,
          },
        });

        if (refreshResp.ok) {
          const refreshData = await refreshResp.json();
          accessToken = refreshData.accessToken || accessToken;

          // 更新数据库中的 token
          await supabase
            .from("auto_booking_tasks")
            .update({
              access_token: accessToken,
              refresh_token: refreshData.refreshToken || task.refresh_token,
            })
            .eq("id", task.id);
        }
      } catch (_) {
        // 刷新失败，使用原有 token
      }

      // 调用选课 API
      const selectResp = await fetch(
        `${UAAA_API_BASE}/api/v1/bykc/courses/${task.course_id}/select`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
          },
        }
      );

      if (selectResp.ok) {
        const data = await selectResp.json();
        await supabase
          .from("auto_booking_tasks")
          .update({
            is_completed: true,
            is_failed: false,
            result_message: data.message || "自动选课成功",
            executed_at: new Date().toISOString(),
          })
          .eq("id", task.id);

        taskResult.status = "success";
        taskResult.message = data.message;
      } else {
        const errorData = await selectResp.json().catch(() => ({}));
        const errorMsg =
          errorData?.details?.message || errorData?.error || `选课失败 (HTTP ${selectResp.status})`;

        await supabase
          .from("auto_booking_tasks")
          .update({
            is_completed: true,
            is_failed: true,
            result_message: errorMsg,
            executed_at: new Date().toISOString(),
          })
          .eq("id", task.id);

        taskResult.status = "failed";
        taskResult.message = errorMsg;
      }
    } catch (err) {
      taskResult.status = "error";
      taskResult.message = String(err);
    }

    results.push(taskResult);
  }

  return new Response(JSON.stringify({ processed: results.length, results }), {
    headers: { "Content-Type": "application/json" },
  });
});
