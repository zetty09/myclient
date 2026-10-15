package com.myclient.visuals;

import com.myclient.Config;
import com.myclient.mixin.BossBarHudAccessor;
import com.myclient.ui.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;

public class DynamicIsland {
    private record Notice(String title, String sub, long durationMs) {}

    private static final Deque<Notice> QUEUE = new ArrayDeque<>();
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");
    private static Notice current;
    private static long shownAt;
    private static long lastNanos = System.nanoTime();
    private static float w = 60, h = 18, curY = 4;

    public static void push(String title, String sub, long durationMs) {
        if (QUEUE.size() > 5) QUEUE.pollFirst();
        QUEUE.addLast(new Notice(title, sub, durationMs));
    }

    /** Ile bossbarow jest teraz na ekranie (kazdy zajmuje 19 px od gory). */
    private static int bossBars(MinecraftClient c) {
        try {
            var hud = c.inGameHud.getBossBarHud();
            return ((BossBarHudAccessor) (Object) hud).aether$getBossBars().size();
        } catch (Throwable t) {
            return 0;
        }
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
        String idle = Config.data.islandName == null || Config.data.islandName.isBlank()
                ? c.getSession().getUsername() : Config.data.islandName;
        String t1 = null, t2 = null;
        float tw, th;
        if (current == null) {
            tw = tr.getWidth(idle) + 22;
            th = 18;
        } else {
            t1 = tr.trimToWidth(current.title(), 190);
            t2 = current.sub() == null ? null : tr.trimToWidth(current.sub(), 190);
            int tx = Math.max(tr.getWidth(t1), t2 == null ? 0 : tr.getWidth(t2));
            tw = tx + 28;
            th = t2 != null ? 30 : 20;
        }

        // sprezyste wygladzanie niezalezne od FPS
        float k = 1f - (float) Math.exp(-dt * 14f);
        w += (tw - w) * k;
        h += (th - h) * k;
        float targetY = 4 + bossBars(c) * 19 - (bossBars(c) > 0 ? 1 : 0);
        curY += (targetY - curY) * k;

        int iw = Math.round(w), ih = Math.round(h);
        int x = (ctx.getScaledWindowWidth() - iw) / 2;
        int y = Math.round(curY);
        Gfx.roundRect(ctx, x, y, iw, ih, ih / 2f, 0xFF000000);

        // godzina po lewej (bez tla, jedzie razem z wyspa)
        String time = LocalTime.now().format(CLOCK);
        ctx.drawText(tr, time, x - 8 - tr.getWidth(time), y + (ih - 8) / 2, 0xFFFFFFFF, true);

        // zasieg po prawej: pelny na serwerze, 0 kresek w singleplayerze
        int level = c.isInSingleplayer() ? 0 : 4;
        int bx = x + iw + 8;
        int bottom = y + ih / 2 + 4;
        for (int i = 0; i < 4; i++) {
            int bh = 3 + 2 * i;
            int color = i < level ? 0xFFFFFFFF : 0x55FFFFFF;
            ctx.fill(bx + i * 3, bottom - bh, bx + i * 3 + 2, bottom, color);
        }

        if (Math.abs(w - tw) >= 5 || Math.abs(h - th) >= 4) return;

        if (current == null) {
            ctx.drawText(tr, idle, x + (iw - tr.getWidth(idle)) / 2, y + (ih - 8) / 2, 0xFFFFFFFF, false);
        } else if (t2 != null) {
            ctx.drawText(tr, t1, x + (iw - tr.getWidth(t1)) / 2, y + 5, 0xFFFFFFFF, false);
            ctx.drawText(tr, t2, x + (iw - tr.getWidth(t2)) / 2, y + 16, 0xFF9AA0A6, false);
        } else {
            ctx.drawText(tr, t1, x + (iw - tr.getWidth(t1)) / 2, y + (ih - 8) / 2, 0xFFFFFFFF, false);
        }
    }
}
