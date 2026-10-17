package com.myclient.ui;

import net.minecraft.client.gui.DrawContext;

/** Male ikonki 8x8 rysowane pikselami (bez plikow graficznych). */
public final class Icons {
    public static final String[] MODULES = {
            "XXX.XXX.",
            "XXX.XXX.",
            "XXX.XXX.",
            "........",
            "XXX.XXX.",
            "XXX.XXX.",
            "XXX.XXX.",
            "........"};
    public static final String[] VISUALS = {
            "........",
            "..XXXX..",
            ".X....X.",
            "XX.XX.XX",
            "XX.XX.XX",
            ".X....X.",
            "..XXXX..",
            "........"};
    public static final String[] COSMETICS = {
            "...XX...",
            "...XX...",
            "..XXXX..",
            "XXXXXXXX",
            "XXXXXXXX",
            "..XXXX..",
            "...XX...",
            "...XX..."};
    public static final String[] INTERFACE = {
            "X.......",
            "XX......",
            "XXX.....",
            "XXXX....",
            "XXXXX...",
            "XXX.....",
            "X.XX....",
            "..XX...."};
    public static final String[] WINGS = {
            "X......X",
            "XX....XX",
            "XXX..XXX",
            "XXXXXXXX",
            ".XXXXXX.",
            "..XXXX..",
            "...XX...",
            "........"};
    public static final String[] HEADBAND = {
            "..XXXX..",
            ".X....X.",
            "XXXXXXXX",
            "XXXXXXXX",
            ".X....X.",
            ".X....X.",
            "..XXXX..",
            "........"};
    public static final String[] FISTS = {
            "..XXXX..",
            ".XXXXXX.",
            "XXXXXXX.",
            "XXXXXXXX",
            "XXXXXXXX",
            ".XXXXXX.",
            "..XXXX..",
            "..XXXX.."};
    public static final String[] CAPE = {
            ".XXXXXX.",
            "XXXXXXXX",
            "XXXXXXXX",
            ".XXXXXX.",
            ".XXXXXX.",
            "..XXXX..",
            "..XXXX..",
            "...XX..."};

    private Icons() {}

    public static void draw(DrawContext ctx, String[] bitmap, int x, int y, int scale, int color) {
        color = Gfx.a(color);
        if ((color >>> 24) == 0) return;
        for (int r = 0; r < bitmap.length; r++) {
            String row = bitmap[r];
            for (int c = 0; c < row.length(); c++) {
                if (row.charAt(c) == 'X') {
                    ctx.fill(x + c * scale, y + r * scale, x + (c + 1) * scale, y + (r + 1) * scale, color);
                }
            }
        }
    }
}
