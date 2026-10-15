package com.myclient.ui;

import com.myclient.ClientInfo;
import com.myclient.Config;
import com.myclient.cosmetics.WingType;
import com.myclient.modules.Module;
import com.myclient.modules.ModuleManager;
import com.myclient.visuals.DynamicIsland;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class MainMenuScreen extends Screen {
    private static final int PW = 420, PH = 250, SIDEBAR = 100;
    private static final int ACCENT = ClientInfo.ACCENT;
    private static final String[] TABS = {"General", "Cosmetics"};
    private static final int ROW_H = 30;

    private int px, py;
    private int tab = 0;
    private Module listening;

    public MainMenuScreen() {
        super(Text.literal(ClientInfo.NAME));
    }

    @Override
    protected void init() {
        px = (width - PW) / 2;
        py = (height - PH) / 2;
    }

    private int cx() { return px + SIDEBAR + 14; }
    private int cw() { return PW - SIDEBAR - 28; }

    private static String keyName(int key) {
        if (key <= 0) return "None";
        try {
            return InputUtil.fromKeyCode(key, -1).getLocalizedText().getString();
        } catch (Exception e) {
            return "Key " + key;
        }
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);

        // panel + sidebar
        Gfx.roundRect(ctx, px, py, PW, PH, 10, 0xF0121218);
        Gfx.roundRect(ctx, px, py, SIDEBAR, PH, 10, 0xF00B0B10);
        ctx.fill(px + SIDEBAR - 10, py, px + SIDEBAR, py + PH, 0xF00B0B10);

        ctx.drawText(textRenderer, ClientInfo.LOGO + " " + ClientInfo.NAME, px + 12, py + 14, 0xFFFFFFFF, true);
        for (int i = 0; i < TABS.length; i++) {
            int ty = py + 40 + i * 26;
            boolean sel = tab == i;
            boolean hover = mouseX >= px + 8 && mouseX < px + SIDEBAR - 8 && mouseY >= ty && mouseY < ty + 22;
            if (sel || hover) Gfx.roundRect(ctx, px + 8, ty, SIDEBAR - 16, 22, 6, sel ? 0xFF1E1B33 : 0xFF15131F);
            if (sel) ctx.fill(px + 8, ty + 6, px + 10, ty + 16, ACCENT);
            ctx.drawText(textRenderer, TABS[i], px + 18, ty + 7, sel ? 0xFFFFFFFF : 0xFFB9B4D6, false);
        }

        ctx.drawText(textRenderer, TABS[tab], cx(), py + 14, 0xFFFFFFFF, true);
        if (tab == 0) drawGeneral(ctx, mouseX, mouseY);
        else drawCosmetics(ctx, mouseX, mouseY);
    }

    // ---------------- General: moduly + bindy ----------------

    private int rowY(int i) { return py + 36 + i * (ROW_H + 4); }
    private int bindX() { return cx() + cw() - 112; }
    private int switchX() { return cx() + cw() - 30; }

    private void drawGeneral(DrawContext ctx, int mx, int my) {
        for (int i = 0; i < ModuleManager.ALL.size(); i++) {
            Module m = ModuleManager.ALL.get(i);
            int y = rowY(i);
            Gfx.roundRect(ctx, cx(), y, cw(), ROW_H, 6, 0xFF1A1829);
            ctx.drawText(textRenderer, m.name, cx() + 8, y + 6, 0xFFFFFFFF, false);
            String desc = textRenderer.trimToWidth(m.description, cw() - 160);
            ctx.drawText(textRenderer, desc, cx() + 8, y + 17, 0xFF6B6785, false);

            // bind
            boolean ls = listening == m;
            boolean bh = mx >= bindX() && mx < bindX() + 74 && my >= y + 8 && my < y + 22;
            Gfx.roundRect(ctx, bindX(), y + 8, 74, 14, 5, ls ? ACCENT : (bh ? 0xFF2E2B47 : 0xFF2A2840));
            String label = ls ? "Press key..." : "Bind: " + keyName(m.key);
            label = textRenderer.trimToWidth(label, 70);
            ctx.drawText(textRenderer, label, bindX() + (74 - textRenderer.getWidth(label)) / 2, y + 11, 0xFFFFFFFF, false);

            // przelacznik
            Gfx.roundRect(ctx, switchX(), y + 10, 22, 11, 5, m.enabled ? ACCENT : 0xFF3A3850);
            int kx = m.enabled ? switchX() + 12 : switchX() + 1;
            Gfx.roundRect(ctx, kx, y + 11, 9, 9, 4, 0xFFFFFFFF);
        }
        ctx.drawText(textRenderer, "Click Bind, then press a key (Backspace = clear, Esc = cancel)",
                cx(), py + PH - 16, 0xFF6B6785, false);
    }

    // ---------------- Cosmetics: skrzydla (reszta w kolejnych etapach) ----------------

    private static final int CW = 60, CH = 84, GAP = 6;
    private int cardX(int i) { return cx() + i * (CW + GAP); }
    private int cardY() { return py + 62; }

    private void drawCosmetics(DrawContext ctx, int mouseX, int mouseY) {
        ctx.drawText(textRenderer, "Wings", cx(), py + 40, 0xFFB9B4D6, false);
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
    }

    // ---------------- Input ----------------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);
        listening = null;

        for (int i = 0; i < TABS.length; i++) {
            int ty = py + 40 + i * 26;
            if (mx >= px + 8 && mx < px + SIDEBAR - 8 && my >= ty && my < ty + 22) {
                tab = i;
                return true;
            }
        }

        if (tab == 0) {
            for (int i = 0; i < ModuleManager.ALL.size(); i++) {
                Module m = ModuleManager.ALL.get(i);
                int y = rowY(i);
                if (my < y || my >= y + ROW_H) continue;
                if (mx >= bindX() && mx < bindX() + 74) {
                    listening = m;
                    return true;
                }
                if (mx >= switchX() - 2 && mx < switchX() + 24) {
                    m.toggle();
                    ModuleManager.save();
                    return true;
                }
            }
        } else {
            WingType[] types = WingType.values();
            for (int i = 0; i < types.length; i++) {
                int x = cardX(i), y = cardY();
                if (mx >= x && mx < x + CW && my >= y && my < y + CH) {
                    Config.data.wings = types[i].name();
                    Config.save();
                    DynamicIsland.push("Wings", types[i].label, 1500);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                // anuluj, nic nie zmieniaj
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                listening.key = -1;
                ModuleManager.save();
            } else {
                listening.key = keyCode;
                ModuleManager.save();
                DynamicIsland.push("Bind", listening.name + " = " + keyName(keyCode), 1500);
            }
            listening = null;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
