package com.myclient.ui;

import com.myclient.ClientInfo;
import com.myclient.Config;
import com.myclient.visuals.HudElement;
import com.myclient.visuals.HudElements;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Edytor HUD: przeciagnij elementy myszka, ESC zapisuje. */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private HudElement drag;
    private int offX, offY;
    private boolean snapX, snapY;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        // linie pomocnicze (srodek ekranu) tylko podczas przeciagania
        if (drag != null) {
            if (snapX) ctx.fill(width / 2, 0, width / 2 + 1, height, 0x880A84FF);
            if (snapY) ctx.fill(0, height / 2, width, height / 2 + 1, 0x880A84FF);
        }

        for (HudElement e : HudElements.ALL) {
            e.render(ctx, client);
            int x = e.x(width), y = e.y(height);
            boolean active = e == drag;
            boolean hover = mouseX >= x && mouseX < x + e.w && mouseY >= y && mouseY < y + e.h;
            int col = !e.enabled() ? 0xFF636366 : (active || hover ? Theme.accent() : 0xAAFFFFFF);
            Gfx.border(ctx, x - 2, y - 2, e.w + 4, e.h + 4, col);
            String tag = e.label + (e.enabled() ? "" : " (off)");
            ctx.drawText(textRenderer, tag, x, y - 12, col, true);
        }

        String hint = "Drag elements to move them  -  ESC to save and go back";
        ctx.drawText(textRenderer, hint, (width - textRenderer.getWidth(hint)) / 2, height - 18, 0xFF8E8E93, true);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (int i = HudElements.ALL.size() - 1; i >= 0; i--) {
                HudElement e = HudElements.ALL.get(i);
                int x = e.x(width), y = e.y(height);
                if (mx >= x && mx < x + e.w && my >= y && my < y + e.h) {
                    drag = e;
                    offX = (int) mx - x;
                    offY = (int) my - y;
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (drag == null) return super.mouseDragged(mx, my, button, dx, dy);
        int nx = (int) mx - offX;
        int ny = (int) my - offY;

        // przyciaganie do srodka i krawedzi (4 px)
        int cx = (width - drag.w) / 2;
        int cy = (height - drag.h) / 2;
        snapX = Math.abs(nx - cx) <= 4;
        snapY = Math.abs(ny - cy) <= 4;
        if (snapX) nx = cx;
        if (snapY) ny = cy;
        if (Math.abs(nx) <= 4) nx = 0;
        if (Math.abs(ny) <= 4) ny = 0;
        if (Math.abs(nx - (width - drag.w)) <= 4) nx = width - drag.w;
        if (Math.abs(ny - (height - drag.h)) <= 4) ny = height - drag.h;

        drag.setPos(nx, ny, width, height);
        return true;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (drag != null) {
            drag = null;
            snapX = snapY = false;
            Config.save();
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public void close() {
        Config.save();
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
