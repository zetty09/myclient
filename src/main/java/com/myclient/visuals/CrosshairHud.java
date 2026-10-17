package com.myclient.visuals;

import com.myclient.modules.CrosshairModule;
import com.myclient.modules.ModuleManager;
import com.myclient.ui.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** Rysuje wlasny crosshair na srodku ekranu. */
public final class CrosshairHud {
    private static float extra = 0f;
    private static long lastNs = System.nanoTime();

    private CrosshairHud() {}

    public static void render(DrawContext ctx, MinecraftClient c) {
        if (!c.options.getPerspective().isFirstPerson()) return;
        CrosshairModule m = ModuleManager.CROSSHAIR;

        long now = System.nanoTime();
        float dt = Math.min((now - lastNs) / 1_000_000_000f, 0.05f);
        lastNs = now;

        // dynamiczny odstep: rosnie przy ruchu, sprincie i w powietrzu
        float target = 0f;
        if (m.dynamic.value && c.player != null) {
            if (c.player.getVelocity().horizontalLength() > 0.01) target += 2f;
            if (c.player.isSprinting()) target += 2f;
            if (!c.player.isOnGround()) target += 3f;
        }
        extra += (target - extra) * (1f - (float) Math.exp(-dt * 12f));

        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        int col = Colors.pick(m.color.get());
        int size = Math.round(m.size.value);
        int gap = Math.round(m.gap.value + extra);
        int th = Math.round(m.thickness.value);
        int lo = -(th / 2), hi = lo + th;
        boolean outline = m.outline.value;
        String style = m.style.get();

        List<int[]> rects = new ArrayList<>();
        switch (style) {
            case "Cross" -> {
                rects.add(new int[]{cx - gap - size, cy + lo, cx - gap, cy + hi});
                rects.add(new int[]{cx + gap, cy + lo, cx + gap + size, cy + hi});
                rects.add(new int[]{cx + lo, cy - gap - size, cx + hi, cy - gap});
                rects.add(new int[]{cx + lo, cy + gap, cx + hi, cy + gap + size});
            }
            case "Dot" -> {
                int d = Math.max(2, th + 1);
                rects.add(new int[]{cx - d / 2, cy - d / 2, cx - d / 2 + d, cy - d / 2 + d});
            }
            case "Square" -> {
                int r = gap + size;
                rects.add(new int[]{cx - r, cy - r, cx + r, cy - r + th});
                rects.add(new int[]{cx - r, cy + r - th, cx + r, cy + r});
                rects.add(new int[]{cx - r, cy - r, cx - r + th, cy + r});
                rects.add(new int[]{cx + r - th, cy - r, cx + r, cy + r});
            }
            default -> { /* Circle rysowany liniami nizej */ }
        }
        if (m.dot.value && !style.equals("Dot")) {
            int d = Math.max(2, th + 1);
            rects.add(new int[]{cx - d / 2, cy - d / 2, cx - d / 2 + d, cy - d / 2 + d});
        }

        if (outline) {
            for (int[] r : rects) ctx.fill(r[0] - 1, r[1] - 1, r[2] + 1, r[3] + 1, 0xFF000000);
        }
        for (int[] r : rects) ctx.fill(r[0], r[1], r[2], r[3], col);

        if (style.equals("Circle")) {
            float rad = gap + size;
            int n = 28;
            float px = cx + rad, py = cy;
            for (int i = 1; i <= n; i++) {
                double a = i / (double) n * Math.PI * 2;
                float nx = (float) (cx + Math.cos(a) * rad), ny = (float) (cy + Math.sin(a) * rad);
                Gfx.line(ctx, px, py, nx, ny, col, th);
                px = nx;
                py = ny;
            }
        }
    }
}
