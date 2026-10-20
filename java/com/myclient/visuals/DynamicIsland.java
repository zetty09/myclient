package com.myclient.visuals;

import com.myclient.Config;
import com.myclient.mixin.BossBarHudAccessor;
import com.myclient.ui.Fonts;
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
        String font = com.myclient.ui.Theme.islandFont();
        String idle = Config.data.islandName == null || Config.data.islandName.isBlank()
                ? c.getSession().getUsername() : Config.data.islandName;
        String t1 = null, t2 = null;
        float tw, th;
        var mm = com.myclient.modules.ModuleManager.MUSIC;
        boolean music = current == null && mm != null && mm.enabled && mm.islandMusic.value
                && MediaSession.isPlaying() && MediaSession.hasTrack();
        String musicText = null;
        boolean showCover = false;
        if (music) {
            String line = mm.lyrics.value ? Lyrics.current(MediaSession.position() + mm.lyricsOffset.value) : null;
            String ttl = MediaSession.artist.isEmpty() ? MediaSession.title : MediaSession.title + " - " + MediaSession.artist;
            musicText = Fonts.trim(tr, line != null ? line : ttl, 210, font);
            showCover = mm.cover.value && MediaSession.cover() != null;
        }
        if (current == null && music) {
            tw = Fonts.width(tr, musicText, font) + 22 + (showCover ? 19 : 0);
            th = 20;
        } else if (current == null) {
            tw = Fonts.width(tr, idle, font) + 22;
            th = 18;
        } else {
            t1 = Fonts.trim(tr, current.title(), 190, font);
            t2 = current.sub() == null ? null : Fonts.trim(tr, current.sub(), 190, font);
            int tx = Math.max(Fonts.width(tr, t1, font), t2 == null ? 0 : Fonts.width(tr, t2, font));
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
        com.myclient.ui.Tex.shadow(ctx, x, y, iw, ih, 0.5f, 3);
        Gfx.roundRect(ctx, x, y, iw, ih, ih / 2f, 0xFF000000);

        // godzina po lewej (bez tla, jedzie razem z wyspa)
        String time = LocalTime.now().format(CLOCK);
        Fonts.draw(ctx, tr, time, x - 8 - Fonts.width(tr, time, font), y + (ih - 8) / 2, 0xFFFFFFFF, true, font);

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

        if (current == null && music) {
            int coverW = showCover ? 19 : 0;
            int cw = coverW + Fonts.width(tr, musicText, font);
            int sx = x + (iw - cw) / 2;
            if (showCover) MediaSession.drawCover(ctx, sx, y + (ih - 14) / 2, 14);
            Fonts.draw(ctx, tr, musicText, sx + coverW, y + (ih - 8) / 2, 0xFFFFFFFF, false, font);
        } else if (current == null) {
            Fonts.draw(ctx, tr, idle, x + (iw - Fonts.width(tr, idle, font)) / 2, y + (ih - 8) / 2, 0xFFFFFFFF, false, font);
        } else if (t2 != null) {
            Fonts.draw(ctx, tr, t1, x + (iw - Fonts.width(tr, t1, font)) / 2, y + 5, 0xFFFFFFFF, false, font);
            Fonts.draw(ctx, tr, t2, x + (iw - Fonts.width(tr, t2, font)) / 2, y + 16, 0xFF9AA0A6, false, font);
        } else {
            Fonts.draw(ctx, tr, t1, x + (iw - Fonts.width(tr, t1, font)) / 2, y + (ih - 8) / 2, 0xFFFFFFFF, false, font);
        }
    }
}
