package com.myclient.visuals;

import com.myclient.modules.CpsModule;
import com.myclient.modules.ModuleManager;
import com.myclient.ui.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Widget: "7 | 5 CPS". */
public class CpsHud extends HudElement {
    public CpsHud() {
        super("cps", "CPS", 0.01f, 0.60f);
    }

    @Override
    public boolean enabled() {
        return ModuleManager.CPS != null && ModuleManager.CPS.enabled;
    }

    @Override
    public void render(DrawContext ctx, MinecraftClient c) {
        CpsModule m = ModuleManager.CPS;
        TextRenderer tr = c.textRenderer;
        String value = m.right.value ? ClickTracker.left() + " | " + ClickTracker.right() : String.valueOf(ClickTracker.left());
        String label = " CPS";
        int uw = 16 + tr.getWidth(value) + tr.getWidth(label), uh = 18;
        float s = m.scale.value;
        w = Math.round(uw * s);
        h = Math.round(uh * s);
        int x = x(ctx.getScaledWindowWidth()), y = y(ctx.getScaledWindowHeight());

        var mat = ctx.getMatrices();
        mat.push();
        mat.translate(x, y, 0);
        mat.scale(s, s, 1f);
        if (m.background.value) {
            int a = Math.round(m.opacity.value / 100f * 255f);
            Gfx.roundRect(ctx, 0, 0, uw, uh, 8, (a << 24) | 0x1C1C1E);
        }
        ctx.drawText(tr, value, 8, 5, 0xFFFFFFFF, true);
        ctx.drawText(tr, label, 8 + tr.getWidth(value), 5, 0xFF8E8E93, true);
        mat.pop();
    }
}
