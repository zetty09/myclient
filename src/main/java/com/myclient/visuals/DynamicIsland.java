package com.myclient.visuals;

import com.myclient.ui.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

public class DynamicIsland {
    private record Notice(String title, String sub, long durationMs) {}

    private static final Deque<Notice> QUEUE = new ArrayDeque<>();
    private static Notice current;
    private static long shownAt;
    private static long lastNanos = System.nanoTime();
    private static float w = 60, h = 18;

    public static void push(String title, String sub, long durationMs) {
        if (QUEUE.size() > 5) QUEUE.pollFirst();
        QUEUE.addLast(new Notice(title, sub, durationMs));
    }

    public static void render(DrawContext ctx, MinecraftClient c) {
        long now = System.nanoTime();
        float dt = Math.min((now - lastNanos) / 1_000_000_000f, 0.1f);
        lastNanos = now;
        long ms = System.currentTimeMillis();

        if (current != null && ms - shownAt > current.durationMs()) current = null;
        if (current == null && !QUEUE.isEmpty()) {
            current = QUEUE.pollFirst();
            shownAt = ms;
        }

        var tr = c.textRenderer;
        String idle = "";
        float tw, th;
        if (current == null) {
            var e = c.getNetworkHandler() == null || c.player == null ? null
                    : c.getNetworkHandler().getPlayerListEntry(c.player.getUuid());
            idle = c.getCurrentFps() + " fps" + (e != null && e.getLatency() > 0 ? "  |  " + e.getLatency() + " ms" : "");
            tw = tr.getWidth(idle) + 22;
            th = 18;
        } else {
            int tx = Math.max(tr.getWidth(current.title()), current.sub() == null ? 0 : tr.getWidth(current.sub()));
            tw = tx + 28;
            th = current.sub() != null ? 30 : 20;
        }

        // sprezyste wygladzanie niezalezne od FPS
        float k = 1f - (float) Math.exp(-dt * 14f);
        w += (tw - w) * k;
        h += (th - h) * k;

        int iw = Math.round(w), ih = Math.round(h);
        int x = (ctx.getScaledWindowWidth() - iw) / 2;
        int y = 4;
        Gfx.roundRect(ctx, x, y, iw, ih, ih / 2f, 0xFF000000);

        if (Math.abs(w - tw) >= 5 || Math.abs(h - th) >= 4) return;

        if (current == null) {
            ctx.drawText(tr, idle, x + (iw - tr.getWidth(idle)) / 2, y + (ih - 8) / 2, 0xFFFFFFFF, false);
        } else if (current.sub() != null) {
            ctx.drawText(tr, current.title(), x + (iw - tr.getWidth(current.title())) / 2, y + 5, 0xFFFFFFFF, false);
            ctx.drawText(tr, current.sub(), x + (iw - tr.getWidth(current.sub())) / 2, y + 16, 0xFF9AA0A6, false);
        } else {
            ctx.drawText(tr, current.title(), x + (iw - tr.getWidth(current.title())) / 2, y + (ih - 8) / 2, 0xFFFFFFFF, false);
        }
    }
}
