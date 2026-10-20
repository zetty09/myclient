package com.myclient.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

/** Ikonki: wygladzone grafiki 128x128 (textures/icons), skalowane z filtrowaniem liniowym. */
public final class Icons {
    private static Identifier id(String n) { return Identifier.of("myclient", "textures/icons/" + n + ".png"); }

    public static final Identifier MODULES = id("modules");
    public static final Identifier VISUALS = id("visuals");
    public static final Identifier COSMETICS = id("cosmetics");
    public static final Identifier INTERFACE = id("interface");
    public static final Identifier WINGS = id("wings");
    public static final Identifier HEADBAND = id("headband");
    public static final Identifier FISTS = id("fists");
    public static final Identifier CAPE = id("cape");
    public static final Identifier NOTE = id("note");
    public static final Identifier PLAY = id("play");
    public static final Identifier PAUSE = id("pause");
    public static final Identifier PREV = id("prev");
    public static final Identifier NEXT = id("next");

    private Icons() {}

    /** Rysuje ikonke w kwadracie size x size (px GUI), tintowana kolorem (z globalna przezroczystoscia). */
    public static void draw(DrawContext ctx, Identifier icon, int x, int y, int size, int color) {
        color = Gfx.a(color);
        if ((color >>> 24) == 0) return;
        var m = ctx.getMatrices();
        m.push();
        m.translate(x, y, 0);
        float s = size / 128f;
        m.scale(s, s, 1f);
        ctx.drawTexture(RenderLayer::getGuiTextured, icon, 0, 0, 0f, 0f, 128, 128, 128, 128, color);
        m.pop();
    }
}
