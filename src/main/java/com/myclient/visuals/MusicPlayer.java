package com.myclient.visuals;

import com.myclient.ui.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/** Prosty odtwarzacz: puszcza muzyke z plyt winylowych z samej gry (bez zewnetrznych plikow). */
public class MusicPlayer {
    private record Track(String title, String sound) {}

    private static final Track[] TRACKS = {
            new Track("C418 - cat", "music_disc.cat"),
            new Track("C418 - blocks", "music_disc.blocks"),
            new Track("C418 - chirp", "music_disc.chirp"),
            new Track("C418 - far", "music_disc.far"),
            new Track("C418 - mall", "music_disc.mall"),
            new Track("C418 - mellohi", "music_disc.mellohi"),
            new Track("C418 - stal", "music_disc.stal"),
            new Track("C418 - strad", "music_disc.strad"),
            new Track("C418 - ward", "music_disc.ward"),
            new Track("C418 - wait", "music_disc.wait"),
            new Track("Lena Raine - otherside", "music_disc.otherside"),
            new Track("Lena Raine - 5", "music_disc.5"),
            new Track("Aaron Cherof - Relic", "music_disc.relic"),
            new Track("Lena Raine - Pigstep", "music_disc.pigstep"),
    };

    private static int idx = 0;
    private static boolean playing = false;
    private static PositionedSoundInstance instance;
    private static int ticksSinceStart = 0;

    public static void play() {
        stop();
        Track t = TRACKS[idx];
        instance = PositionedSoundInstance.master(SoundEvent.of(Identifier.ofVanilla(t.sound())), 1.0f, 1.0f);
        MinecraftClient.getInstance().getSoundManager().play(instance);
        playing = true;
        ticksSinceStart = 0;
        DynamicIsland.push("Now playing", t.title(), 2500);
    }

    public static void stop() {
        if (instance != null) MinecraftClient.getInstance().getSoundManager().stop(instance);
        instance = null;
        playing = false;
    }

    private static void next() { idx = (idx + 1) % TRACKS.length; if (playing) play(); }
    private static void prev() { idx = (idx - 1 + TRACKS.length) % TRACKS.length; if (playing) play(); }

    public static void tick(MinecraftClient c) {
        if (!playing || instance == null) return;
        ticksSinceStart++;
        if (ticksSinceStart > 60 && !c.getSoundManager().isPlaying(instance)) next(); // koniec utworu
    }

    // --- uklad karty (prawy dolny rog) ---
    private static final int CW = 126, CH = 44;
    private static int cx(int sw) { return sw - CW - 8; }
    private static int cy(int sh) { return sh - CH - 8; }
    private static int bx(int sw, int i) { return cx(sw) + 8 + i * 38; }
    private static int by(int sh) { return cy(sh) + 29; }

    public static void render(DrawContext ctx, MinecraftClient c) {
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        int x = cx(sw), y = cy(sh);
        var tr = c.textRenderer;
        Gfx.roundRect(ctx, x, y, CW, CH, 8, 0xE0000000);
        ctx.drawText(tr, playing ? "Now playing" : "Paused", x + 8, y + 5, 0xFF9AA0A6, false);
        String title = tr.trimToWidth(TRACKS[idx].title(), CW - 16);
        ctx.drawText(tr, title, x + 8, y + 16, 0xFFFFFFFF, false);

        String[] labels = {"|<", playing ? "II" : ">", ">|"};
        for (int i = 0; i < 3; i++) {
            int bxx = bx(sw, i);
            Gfx.roundRect(ctx, bxx, by(sh), 34, 12, 5, i == 1 ? 0xFF7C5CFF : 0xFF2A2840);
            ctx.drawText(tr, labels[i], bxx + (34 - tr.getWidth(labels[i])) / 2, by(sh) + 2, 0xFFFFFFFF, false);
        }
    }

    /** Klik w przyciski karty (dziala gdy czat jest otwarty). Zwraca true jesli klik zostal obsluzony. */
    public static boolean click(double mx, double my, int sw, int sh) {
        for (int i = 0; i < 3; i++) {
            int x = bx(sw, i), y = by(sh);
            if (mx >= x && mx < x + 34 && my >= y && my < y + 12) {
                if (i == 0) prev();
                else if (i == 2) next();
                else if (playing) stop(); else play();
                return true;
            }
        }
        return false;
    }
}
