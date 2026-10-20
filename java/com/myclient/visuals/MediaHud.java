package com.myclient.visuals;

import com.myclient.modules.ModuleManager;
import com.myclient.modules.MusicModule;
import com.myclient.ui.Gfx;
import com.myclient.ui.Icons;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

/** Przesuwana karta "teraz gra" w stylu iOS. Klikalna, gdy otwarty jest czat. */
public class MediaHud extends HudElement {
    private static final int W = 160;
    // ostatnio narysowane przyciski (wspolrzedne ekranu) do obslugi klikniec
    private int ctrlX, ctrlY, ctrlStep, ctrlSize;
    private boolean ctrlValid;

    public MediaHud() {
        super("music", "Music Player", 0.99f, 0.97f);
    }

    @Override
    public boolean enabled() {
        return ModuleManager.MUSIC != null && ModuleManager.MUSIC.enabled;
    }

    private static String time(double sec) {
        int s = (int) Math.max(0, sec);
        return (s / 60) + ":" + (s % 60 < 10 ? "0" : "") + (s % 60);
    }

    @Override
    public void render(DrawContext ctx, MinecraftClient c) {
        MusicModule m = ModuleManager.MUSIC;
        TextRenderer tr = c.textRenderer;
        boolean track = MediaSession.hasTrack();

        int uh = 8 + 34 + 8;
        if (m.progress.value) uh += 11;
        if (m.controls.value) uh += 16;
        float s = m.scale.value;
        this.w = Math.round(W * s);
        this.h = Math.round(uh * s);
        int x = x(ctx.getScaledWindowWidth());
        int y = y(ctx.getScaledWindowHeight());

        String title, sub;
        if (!MediaSession.SUPPORTED) { title = "Not available"; sub = "Windows only"; }
        else if (MediaSession.error) { title = "No media access"; sub = "Windows media API failed"; }
        else if (!track) { title = "Nothing playing"; sub = "Open Spotify or Apple Music"; }
        else { title = MediaSession.title; sub = MediaSession.artist.isEmpty() ? MediaSession.app : MediaSession.artist; }

        var mat = ctx.getMatrices();
        mat.push();
        mat.translate(x, y, 0);
        mat.scale(s, s, 1f);

        int a = Math.round(m.opacity.value / 100f * 255f);
        Gfx.roundRect(ctx, 0, 0, W, uh, 12, (a << 24) | 0x1C1C1E);

        // okladka: kolorowy kafelek zalezny od tytulu
        int art;
        if (track) {
            float hue = (Math.abs(title.hashCode()) % 360) / 360f;
            art = 0xFF000000 | MathHelper.hsvToRgb(hue, 0.55f, 0.85f);
        } else {
            art = 0xFF3A3A3C;
        }
        if (!(m.cover.value && track && MediaSession.drawCover(ctx, 8, 8, 34))) {
            Gfx.roundRect(ctx, 8, 8, 34, 34, 9, art);
            Gfx.roundRect(ctx, 8, 8, 34, 17, 9, Gfx.lerpColor(art, 0xFFFFFFFF, 0.18f));
            Icons.draw(ctx, Icons.NOTE, 15, 15, 20, 0xFFFFFFFF);
        }

        int tw = W - 50 - 10;
        ctx.drawText(tr, tr.trimToWidth(title, tw), 50, 11, 0xFFFFFFFF, false);
        ctx.drawText(tr, tr.trimToWidth(sub, tw), 50, 23, 0xFF8E8E93, false);
        if (track && !MediaSession.app.isEmpty()) {
            ctx.drawText(tr, tr.trimToWidth(MediaSession.app, tw), 50, 33, 0xFF636366, false);
        }

        int cy = 8 + 34 + 6;
        if (m.progress.value) {
            double d = MediaSession.duration();
            float frac = track && d > 0 ? (float) Math.min(1.0, MediaSession.position() / d) : 0f;
            Gfx.roundRect(ctx, 8, cy + 2, W - 16, 3, 1, 0xFF48484A);
            int fw = Math.round((W - 16) * frac);
            if (fw > 0) Gfx.roundRect(ctx, 8, cy + 2, Math.max(3, fw), 3, 1, 0xFFFFFFFF);
            cy += 11;
        }
        ctrlValid = false;
        if (m.controls.value) {
            int size = 12, step = 34;
            int total = step * 2 + size;
            int bx = (W - total) / 2;
            Icons.draw(ctx, Icons.PREV, bx, cy, size, 0xFFFFFFFF);
            Icons.draw(ctx, MediaSession.isPlaying() ? Icons.PAUSE : Icons.PLAY, bx + step, cy, size, 0xFFFFFFFF);
            Icons.draw(ctx, Icons.NEXT, bx + step * 2, cy, size, 0xFFFFFFFF);
            ctrlX = Math.round(x + bx * s);
            ctrlY = Math.round(y + cy * s);
            ctrlStep = Math.round(step * s);
            ctrlSize = Math.round(size * s);
            ctrlValid = true;
        }
        mat.pop();
    }

    /** Klik w przyciski (wspolrzedne ekranu). True, gdy trafiono. */
    public boolean click(double mx, double my) {
        if (!enabled() || !ctrlValid || !MediaSession.running()) return false;
        int pad = Math.max(3, ctrlSize / 3);
        for (int i = 0; i < 3; i++) {
            int bx = ctrlX + ctrlStep * i;
            if (mx >= bx - pad && mx < bx + ctrlSize + pad && my >= ctrlY - pad && my < ctrlY + ctrlSize + pad) {
                if (i == 0) MediaSession.prev();
                else if (i == 1) MediaSession.toggle();
                else MediaSession.next();
                return true;
            }
        }
        return false;
    }
}
