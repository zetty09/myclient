package com.myclient.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class Gfx {
    /** Globalna przezroczystosc (0..1) dla animacji wejscia. Zawsze wracaj do 1 po uzyciu. */
    public static float alpha = 1f;

    public static int a(int color) {
        if (alpha >= 0.999f) return color;
        int al = Math.round(((color >>> 24) & 0xFF) * Math.max(0f, alpha));
        return (al << 24) | (color & 0xFFFFFF);
    }

    public static int lerpColor(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int al = (int) (((c1 >>> 24) & 255) * (1 - t) + ((c2 >>> 24) & 255) * t);
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (al << 24) | (r << 16) | (g << 8) | b;
    }

    /** Tekst z uwzglednieniem globalnej przezroczystosci. */
    public static void text(DrawContext ctx, TextRenderer tr, String s, int x, int y, int color, boolean shadow) {
        int c = a(color);
        if ((c >>> 24) < 6) return; // ponizej 4 MC rysuje tekst jako nieprzezroczysty
        ctx.drawText(tr, s, x, y, c, shadow);
    }

    /** Zaokraglony prostokat rysowany liniami (bez tekstur). */
    public static void roundRect(DrawContext ctx, int x, int y, int w, int h, float r, int color) {
        color = a(color);
        if ((color >>> 24) == 0) return;
        r = Math.min(r, Math.min(w, h) / 2f);
        int ri = (int) Math.ceil(r);
        int topRows = Math.min(ri, h);
        for (int row = 0; row < topRows; row++) {
            int o = offset(row, h, r);
            ctx.fill(x + o, y + row, x + w - o, y + row + 1, color);
        }
        int midStart = ri, midEnd = h - ri;
        if (midEnd > midStart) ctx.fill(x, y + midStart, x + w, y + midEnd, color);
        for (int row = Math.max(h - ri, ri); row < h; row++) {
            int o = offset(row, h, r);
            ctx.fill(x + o, y + row, x + w - o, y + row + 1, color);
        }
    }

    private static int offset(int row, int h, float r) {
        float dy = row + 0.5f;
        float off = 0;
        if (dy < r) off = r - (float) Math.sqrt(r * r - (r - dy) * (r - dy));
        else if (dy > h - r) off = r - (float) Math.sqrt(r * r - (dy - (h - r)) * (dy - (h - r)));
        return Math.round(off);
    }

    public static void border(DrawContext ctx, int x, int y, int w, int h, int color) {
        color = a(color);
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    /** Przelacznik w stylu iOS. t: 0 = wylaczony, 1 = wlaczony (animowane). */
    public static void iosSwitch(DrawContext ctx, int x, int y, float t) {
        roundRect(ctx, x, y, 30, 16, 8, lerpColor(0xFF39393D, 0xFF30D158, t));
        roundRect(ctx, x + 2 + Math.round(14 * t), y + 2, 12, 12, 6, 0xFFFFFFFF);
    }
}
