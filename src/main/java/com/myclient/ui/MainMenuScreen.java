package com.myclient.ui;

import com.myclient.Config;
import com.myclient.cosmetics.WingType;
import com.myclient.visuals.DynamicIsland;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;

public class MainMenuScreen extends Screen {
    private static final int PW = 380, PH = 220, SIDEBAR = 96;
    private static final int CW = 60, CH = 84, GAP = 6;
    private static final int ACCENT = 0xFF7C5CFF;

    private int px, py;

    public MainMenuScreen() {
        super(Text.literal("MyClient"));
    }

    @Override
    protected void init() {
        px = (width - PW) / 2;
        py = (height - PH) / 2;
    }

    private int cardX(int i) { return px + SIDEBAR + 14 + i * (CW + GAP); }
    private int cardY() { return py + 62; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        // panel
        Gfx.roundRect(ctx, px, py, PW, PH, 10, 0xF0121218);
        Gfx.roundRect(ctx, px, py, SIDEBAR, PH, 10, 0xF00B0B10);
        ctx.fill(px + SIDEBAR - 10, py, px + SIDEBAR, py + PH, 0xF00B0B10);

        // sidebar: tytul + jedyna zakladka
        ctx.drawText(textRenderer, "MyClient", px + 12, py + 14, 0xFFFFFFFF, true);
        Gfx.roundRect(ctx, px + 8, py + 40, SIDEBAR - 16, 22, 6, 0xFF1E1B33);
        ctx.fill(px + 8, py + 46, px + 10, py + 56, ACCENT);
        ctx.drawText(textRenderer, "Cosmetics", px + 18, py + 47, 0xFFFFFFFF, false);

        // tresc
        ctx.drawText(textRenderer, "Cosmetics", px + SIDEBAR + 14, py + 14, 0xFFFFFFFF, true);
        ctx.drawText(textRenderer, "Wings", px + SIDEBAR + 14, py + 40, 0xFFB9B4D6, false);

        WingType[] types = WingType.values();
        for (int i = 0; i < types.length; i++) {
            WingType t = types[i];
            int x = cardX(i), y = cardY();
            boolean selected = Config.wing() == t;
            boolean hover = mouseX >= x && mouseX < x + CW && mouseY >= y && mouseY < y + CH;

            Gfx.roundRect(ctx, x, y, CW, CH, 6, hover ? 0xFF26233F : 0xFF1A1829);
            if (selected) Gfx.border(ctx, x, y, CW, CH, ACCENT);

            if (t == WingType.NONE) {
                ctx.drawText(textRenderer, "-", x + CW / 2 - 2, y + 30, 0xFF6B6785, false);
            } else {
                var m = ctx.getMatrices();
                m.push();
                m.translate(x + 6, y + 8, 0);
                m.scale(1.5f, 1.5f, 1f);
                ctx.drawTexture(RenderLayer::getGuiTextured, t.texture, 0, 0, 0f, 0f, 32, 32, 32, 32);
                m.pop();
            }
            int lw = textRenderer.getWidth(t.label);
            ctx.drawText(textRenderer, t.label, x + (CW - lw) / 2, y + CH - 14, selected ? 0xFFFFFFFF : 0xFFB9B4D6, false);
        }

        ctx.drawText(textRenderer, "Prawy klik w czacie = lista wizuali", px + SIDEBAR + 14, py + PH - 16, 0xFF6B6785, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            WingType[] types = WingType.values();
            for (int i = 0; i < types.length; i++) {
                int x = cardX(i), y = cardY();
                if (mx >= x && mx < x + CW && my >= y && my < y + CH) {
                    Config.wings = types[i].name();
                    Config.save();
                    DynamicIsland.push("Wings", types[i].label, 1500);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
