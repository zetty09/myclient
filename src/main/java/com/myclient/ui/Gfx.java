package com.myclient.ui;

import net.minecraft.client.gui.DrawContext;

public class Gfx {
    /** Zaokraglony prostokat rysowany liniami (bez tekstur). */
    public static void roundRect(DrawContext ctx, int x, int y, int w, int h, float r, int color) {
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
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }
}
