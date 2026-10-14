package com.myclient.ui;

import com.myclient.Config;
import com.myclient.visuals.DynamicIsland;
import com.myclient.visuals.MusicPlayer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;

/** Lista wizuali wyskakujaca po prawym kliknieciu w czacie. */
public class ChatMenu {
    private static final int ROW = 18, WIDTH = 112, HEADER = 16;

    private static boolean open = false;
    private static int mx0, my0;

    private record Entry(String name, java.util.function.BooleanSupplier on, Runnable toggle) {}

    private static final Entry[] ENTRIES = {
            new Entry("Dynamic Island", () -> Config.island, () -> {
                Config.island = !Config.island;
                if (Config.island) DynamicIsland.push("Dynamic Island", "ON", 1200);
            }),
            new Entry("Music Player", () -> Config.music, () -> Config.music = !Config.music),
    };

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof ChatScreen) {
                open = false;
                ScreenMouseEvents.allowMouseClick(screen).register(ChatMenu::onClick);
                ScreenEvents.afterRender(screen).register(ChatMenu::render);
            }
        });
    }

    private static int height() { return HEADER + ENTRIES.length * ROW + 4; }

    private static boolean onClick(Screen screen, double mx, double my, int button) {
        // klikniecia w odtwarzacz (HUD) gdy czat jest otwarty
        if (Config.music && button == 0 && MusicPlayer.click(mx, my, screen.width, screen.height)) return false;

        if (open) {
            boolean inside = mx >= mx0 && mx < mx0 + WIDTH && my >= my0 && my < my0 + height();
            if (inside && button == 0) {
                int idx = (int) ((my - my0 - HEADER) / ROW);
                if (idx >= 0 && idx < ENTRIES.length) {
                    ENTRIES[idx].toggle().run();
                    Config.save();
                }
                return false;
            }
            if (inside) return false;
            open = false;
            if (button != 1) return true; // klik poza menu zamyka je i leci dalej
        }
        if (button == 1 && my < screen.height - 14) { // nie nad polem tekstowym
            mx0 = (int) Math.min(mx, screen.width - WIDTH - 2);
            my0 = (int) Math.min(my, screen.height - height() - 18);
            open = true;
            return false;
        }
        return true;
    }

    private static void render(Screen screen, DrawContext ctx, int mouseX, int mouseY, float tickDelta) {
        if (!open) return;
        var tr = MinecraftClient.getInstance().textRenderer;
        Gfx.roundRect(ctx, mx0, my0, WIDTH, height(), 6, 0xF0121218);
        ctx.drawText(tr, "Visuals", mx0 + 8, my0 + 5, 0xFFB9B4D6, false);

        for (int i = 0; i < ENTRIES.length; i++) {
            int y = my0 + HEADER + i * ROW;
            boolean hover = mouseX >= mx0 && mouseX < mx0 + WIDTH && mouseY >= y && mouseY < y + ROW;
            if (hover) Gfx.roundRect(ctx, mx0 + 3, y, WIDTH - 6, ROW - 1, 4, 0xFF26233F);
            ctx.drawText(tr, ENTRIES[i].name(), mx0 + 8, y + 5, 0xFFFFFFFF, false);

            boolean on = ENTRIES[i].on().getAsBoolean();
            int sx = mx0 + WIDTH - 28, sy = y + 4;
            Gfx.roundRect(ctx, sx, sy, 20, 10, 5, on ? 0xFF7C5CFF : 0xFF3A3850);
            int kx = on ? sx + 11 : sx + 1;
            Gfx.roundRect(ctx, kx, sy + 1, 8, 8, 4, 0xFFFFFFFF);
        }
    }
}
