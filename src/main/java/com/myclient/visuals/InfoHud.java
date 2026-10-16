package com.myclient.visuals;

import com.myclient.modules.InfoModule;
import com.myclient.modules.ModuleManager;
import com.myclient.ui.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Widget: FPS / Ping / TPS / koordy w jednym miejscu. */
public class InfoHud extends HudElement {
    private static final int GREEN = 0xFF30D158, YELLOW = 0xFFFFD60A, RED = 0xFFFF453A;
    private static final int LABEL = 0xFF8E8E93, WHITE = 0xFFFFFFFF;

    private record Item(String label, String value, int color) {}

    public InfoHud() {
        super("info", "Information", 0.01f, 0.02f);
    }

    @Override
    public boolean enabled() {
        return ModuleManager.INFO != null && ModuleManager.INFO.enabled;
    }

    private static int latency(MinecraftClient c) {
        if (c.getNetworkHandler() == null || c.player == null) return -1;
        var e = c.getNetworkHandler().getPlayerListEntry(c.player.getUuid());
        return e == null ? -1 : e.getLatency();
    }

    @Override
    public void render(DrawContext ctx, MinecraftClient c) {
        InfoModule m = ModuleManager.INFO;
        TextRenderer tr = c.textRenderer;

        List<Item> items = new ArrayList<>();
        if (m.fps.value) {
            int f = c.getCurrentFps();
            items.add(new Item("FPS", String.valueOf(f), f >= 60 ? GREEN : f >= 30 ? YELLOW : RED));
        }
        if (m.ping.value) {
            int p = latency(c);
            items.add(new Item("Ping", p > 0 ? p + " ms" : "-", p <= 0 ? WHITE : p < 80 ? GREEN : p < 150 ? YELLOW : RED));
        }
        if (m.tps.value) {
            float t = TpsTracker.get();
            items.add(new Item("TPS", t < 0 ? "--" : String.format(Locale.ROOT, "%.1f", t),
                    t < 0 ? WHITE : t >= 19f ? GREEN : t >= 15f ? YELLOW : RED));
        }
        if (m.coords.value && c.player != null) {
            items.add(new Item("XYZ", c.player.getBlockX() + " " + c.player.getBlockY() + " " + c.player.getBlockZ(), WHITE));
        }
        if (items.isEmpty()) items.add(new Item("Info", "nothing selected", LABEL));

        int uw, uh;
        if (m.horizontal.value) {
            int total = 12;
            for (Item it : items) total += tr.getWidth(it.label()) + 4 + tr.getWidth(it.value()) + 10;
            uw = total - 6;
            uh = 18;
        } else {
            int widest = 0;
            for (Item it : items) widest = Math.max(widest, tr.getWidth(it.label()) + 10 + tr.getWidth(it.value()));
            uw = widest + 16;
            uh = items.size() * 11 + 7;
        }

        float s = m.scale.value;
        this.w = Math.round(uw * s);
        this.h = Math.round(uh * s);
        int x = x(ctx.getScaledWindowWidth());
        int y = y(ctx.getScaledWindowHeight());

        var mat = ctx.getMatrices();
        mat.push();
        mat.translate(x, y, 0);
        mat.scale(s, s, 1f);

        if (m.background.value) {
            int a = Math.round(m.opacity.value / 100f * 255f);
            Gfx.roundRect(ctx, 0, 0, uw, uh, 8, (a << 24) | 0x1C1C1E);
        }
        if (m.horizontal.value) {
            int cx = 8;
            for (Item it : items) {
                ctx.drawText(tr, it.label(), cx, 5, LABEL, true);
                cx += tr.getWidth(it.label()) + 4;
                ctx.drawText(tr, it.value(), cx, 5, it.color(), true);
                cx += tr.getWidth(it.value()) + 10;
            }
        } else {
            int cy = 4;
            for (Item it : items) {
                ctx.drawText(tr, it.label(), 8, cy, LABEL, true);
                ctx.drawText(tr, it.value(), uw - 8 - tr.getWidth(it.value()), cy, it.color(), true);
                cy += 11;
            }
        }
        mat.pop();
    }
}
