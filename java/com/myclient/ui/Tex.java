package com.myclient.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

/**
 * Teksturowane ozdobniki GUI (assets/myclient/textures/gui): miekki cien, poswiata i polysk.
 * Cien i poswiata to "9-slice": rogi 1:1, krawedzie rozciagniete, srodek pominiety (przykrywa go panel).
 */
public final class Tex {
    private static final Identifier SHADOW = Identifier.of("myclient", "textures/gui/shadow.png");
    private static final Identifier GLOW = Identifier.of("myclient", "textures/gui/glow.png");
    private static final Identifier SHEEN = Identifier.of("myclient", "textures/gui/sheen.png");
    private static final int T = 96, M = 32;

    private Tex() {}

    /** Miekki cien pod prostokatem (x,y,w,h); strength 0..1, offsetY przesuwa cien w dol. */
    public static void shadow(DrawContext ctx, int x, int y, int w, int h, float strength, int offsetY) {
        slice(ctx, SHADOW, x, y + offsetY, w, h, 0x000000, strength);
    }

    /** Kolorowa poswiata wokol prostokata. */
    public static void glow(DrawContext ctx, int x, int y, int w, int h, int rgb, float strength) {
        slice(ctx, GLOW, x, y, w, h, rgb, strength);
    }

    /** Delikatny jasny polysk (gradient od gory) na prostokacie. */
    public static void sheen(DrawContext ctx, int x, int y, int w, int h, float strength) {
        int a = Math.round(255 * Math.max(0f, Math.min(1f, strength)));
        int color = Gfx.a((a << 24) | 0xFFFFFF);
        if ((color >>> 24) == 0) return;
        piece(ctx, SHEEN, x, y, 0, 0, 8, 64, w, h, 8, 64, color);
    }

    private static void slice(DrawContext ctx, Identifier id, int x, int y, int w, int h, int rgb, float strength) {
        int a = Math.round(255 * Math.max(0f, Math.min(1f, strength)));
        int color = Gfx.a((a << 24) | (rgb & 0xFFFFFF));
        if ((color >>> 24) == 0) return;
        int X = x - M, Y = y - M, W = w + 2 * M, H = h + 2 * M;
        int mid = T - 2 * M;
        // rogi
        piece(ctx, id, X, Y, 0, 0, M, M, M, M, T, T, color);
        piece(ctx, id, X + W - M, Y, T - M, 0, M, M, M, M, T, T, color);
        piece(ctx, id, X, Y + H - M, 0, T - M, M, M, M, M, T, T, color);
        piece(ctx, id, X + W - M, Y + H - M, T - M, T - M, M, M, M, M, T, T, color);
        // krawedzie
        piece(ctx, id, X + M, Y, M, 0, mid, M, W - 2 * M, M, T, T, color);
        piece(ctx, id, X + M, Y + H - M, M, T - M, mid, M, W - 2 * M, M, T, T, color);
        piece(ctx, id, X, Y + M, 0, M, M, mid, M, H - 2 * M, T, T, color);
        piece(ctx, id, X + W - M, Y + M, T - M, M, M, mid, M, H - 2 * M, T, T, color);
    }

    /** Rysuje fragment tekstury (u,v,rw,rh) rozciagniety do (dw,dh) w punkcie (dx,dy). */
    private static void piece(DrawContext ctx, Identifier id, int dx, int dy, int u, int v, int rw, int rh,
                              int dw, int dh, int texW, int texH, int color) {
        if (dw <= 0 || dh <= 0) return;
        var m = ctx.getMatrices();
        m.push();
        m.translate(dx, dy, 0);
        m.scale(dw / (float) rw, dh / (float) rh, 1f);
        ctx.drawTexture(RenderLayer::getGuiTextured, id, 0, 0, (float) u, (float) v, rw, rh, texW, texH, color);
        m.pop();
    }
}
