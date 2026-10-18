package com.myclient.visuals;

import com.myclient.modules.KeystrokesModule;
import com.myclient.modules.ModuleManager;
import com.myclient.ui.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

/** Widget: WASD + spacja + LMB/RMB z animacja wcisniecia. */
public class KeystrokesHud extends HudElement {
    private static final int K = 22, G = 2, TOTAL = K * 3 + G * 2;
    private final float[] t = new float[7];
    private long lastNs = System.nanoTime();

    public KeystrokesHud() {
        super("keys", "Keystrokes", 0.01f, 0.40f);
    }

    @Override
    public boolean enabled() {
        return ModuleManager.KEYSTROKES != null && ModuleManager.KEYSTROKES.enabled;
    }

    private void key(DrawContext ctx, TextRenderer tr, int i, KeyBinding kb, int x, int y, int w, int h,
                     String label, String sub, int baseAlpha, float dt) {
        boolean down = kb != null && kb.isPressed();
        t[i] += ((down ? 1f : 0f) - t[i]) * (1f - (float) Math.exp(-dt * 18f));
        int bg = Gfx.lerpColor((baseAlpha << 24) | 0x000000, 0xF2FFFFFF, t[i]);
        Gfx.roundRect(ctx, x, y, w, h, 6, bg);
        int fg = Gfx.lerpColor(0xFFFFFFFF, 0xFF1C1C1E, t[i]);
        if (sub == null) {
            ctx.drawText(tr, label, x + (w - tr.getWidth(label)) / 2, y + (h - 8) / 2, fg, false);
        } else {
            ctx.drawText(tr, label, x + (w - tr.getWidth(label)) / 2, y + 4, fg, false);
            int sc = Gfx.lerpColor(0xFF8E8E93, 0xFF48484A, t[i]);
            ctx.drawText(tr, sub, x + (w - tr.getWidth(sub)) / 2, y + 14, sc, false);
        }
    }

    @Override
    public void render(DrawContext ctx, MinecraftClient c) {
        KeystrokesModule m = ModuleManager.KEYSTROKES;
        TextRenderer tr = c.textRenderer;
        long now = System.nanoTime();
        float dt = Math.min((now - lastNs) / 1_000_000_000f, 0.05f);
        lastNs = now;

        int uh = K * 2 + G;
        if (m.mouse.value) uh += G + 26;
        if (m.space.value) uh += G + 14;
        float s = m.scale.value;
        w = Math.round(TOTAL * s);
        h = Math.round(uh * s);
        int x = x(ctx.getScaledWindowWidth()), y = y(ctx.getScaledWindowHeight());
        int a = Math.round(m.opacity.value / 100f * 255f);
        var o = c.options;

        var mat = ctx.getMatrices();
        mat.push();
        mat.translate(x, y, 0);
        mat.scale(s, s, 1f);

        key(ctx, tr, 0, o.forwardKey, K + G, 0, K, K, "W", null, a, dt);
        key(ctx, tr, 1, o.leftKey, 0, K + G, K, K, "A", null, a, dt);
        key(ctx, tr, 2, o.backKey, K + G, K + G, K, K, "S", null, a, dt);
        key(ctx, tr, 3, o.rightKey, (K + G) * 2, K + G, K, K, "D", null, a, dt);
        int cy = K * 2 + G * 2;
        if (m.mouse.value) {
            int bw = (TOTAL - G) / 2;
            String l = m.cps.value ? String.valueOf(ClickTracker.left()) : null;
            String r = m.cps.value ? String.valueOf(ClickTracker.right()) : null;
            key(ctx, tr, 4, o.attackKey, 0, cy, bw, 26, "LMB", l, a, dt);
            key(ctx, tr, 5, o.useKey, bw + G, cy, TOTAL - bw - G, 26, "RMB", r, a, dt);
            cy += 26 + G;
        }
        if (m.space.value) {
            key(ctx, tr, 6, o.jumpKey, 0, cy, TOTAL, 14, "", null, a, dt);
            // symbol spacji: pozioma linia
            int lc = Gfx.lerpColor(0xFFFFFFFF, 0xFF1C1C1E, t[6]);
            ctx.fill(TOTAL / 2 - 9, cy + 7, TOTAL / 2 + 9, cy + 8, lc);
        }
        mat.pop();
    }
}
