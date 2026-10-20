package com.myclient.ui;

import com.myclient.modules.Module;
import com.myclient.modules.ModuleManager;
import com.myclient.visuals.HudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;

/** Lista visuali wyskakujaca po prawym kliknieciu w czacie (+ skrot do edytora HUD). */
public class ChatMenu {
    private static final int ROW = 20, WIDTH = 140, HEADER = 20;

    private static boolean open = false;
    private static int mx0, my0;

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof ChatScreen) {
                open = false;
                ScreenMouseEvents.allowMouseClick(screen).register(ChatMenu::onClick);
                ScreenEvents.afterRender(screen).register(ChatMenu::render);
            }
        });
    }

    private static List<Module> visuals() {
        return ModuleManager.of(Module.Category.VISUAL);
    }

    /** Wiersze: visuale + "Edit HUD positions". */
    private static int rows() { return visuals().size() + 1; }

    private static int height() { return HEADER + rows() * ROW + 6; }

    private static boolean onClick(Screen screen, double mx, double my, int button) {
        // klikniecia w odtwarzacz (HUD) gdy czat jest otwarty
        if (ModuleManager.MUSIC.enabled && button == 0 && HudElements.MEDIA.click(mx, my)) return false;

        if (open) {
            boolean inside = mx >= mx0 && mx < mx0 + WIDTH && my >= my0 && my < my0 + height();
            if (inside && button == 0) {
                int idx = (int) ((my - my0 - HEADER) / ROW);
                List<Module> vis = visuals();
                if (idx >= 0 && idx < vis.size()) {
                    vis.get(idx).toggle();
                    ModuleManager.save();
                } else if (idx == vis.size()) {
                    open = false;
                    MinecraftClient.getInstance().setScreen(new HudEditorScreen(screen));
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
        Gfx.roundRect(ctx, mx0, my0, WIDTH, height(), 12, 0xF01C1C1E);
        ctx.drawText(tr, "Visuals", mx0 + 10, my0 + 7, 0xFF8E8E93, false);

        List<Module> vis = visuals();
        for (int i = 0; i < rows(); i++) {
            int y = my0 + HEADER + i * ROW;
            boolean hover = mouseX >= mx0 && mouseX < mx0 + WIDTH && mouseY >= y && mouseY < y + ROW;
            if (hover) Gfx.roundRect(ctx, mx0 + 4, y, WIDTH - 8, ROW - 2, 7, 0xFF3A3A3C);

            if (i < vis.size()) {
                Module m = vis.get(i);
                ctx.drawText(tr, m.name, mx0 + 10, y + 6, 0xFFFFFFFF, false);
                Gfx.iosSwitch(ctx, mx0 + WIDTH - 40, y + 1, m.enabled ? 1f : 0f);
            } else {
                ctx.drawText(tr, "Edit HUD positions", mx0 + 10, y + 6, 0xFF0A84FF, false);
            }
        }
    }
}
