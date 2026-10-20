package com.myclient.ui;

import com.myclient.modules.ModuleManager;

/** Motywy kolorystyczne menu klienta (wybierane w module UI Theme). */
public final class Theme {
    public record Palette(String name, int accent, int panel, int sidebar, int card, int hover) {}

    public static final Palette[] ALL = {
            new Palette("iOS Dark", 0xFF0A84FF, 0xF01C1C1E, 0xF0141416, 0xFF2C2C2E, 0xFF3A3A3C),
            new Palette("Midnight", 0xFF6C6AF0, 0xF00F1020, 0xF00A0B16, 0xFF1B1D33, 0xFF272A47),
            new Palette("Amethyst", 0xFFBF5AF2, 0xF01E1626, 0xF0150F1B, 0xFF2D2238, 0xFF3B2E4A),
            new Palette("Rose", 0xFFFF375F, 0xF0221619, 0xF0190F12, 0xFF33222A, 0xFF442D37),
            new Palette("Forest", 0xFF30D158, 0xF0141C17, 0xF00E1410, 0xFF1F2E25, 0xFF2A3E32),
            new Palette("Ocean", 0xFF64D2FF, 0xF010202A, 0xF00B1720, 0xFF1A3140, 0xFF244456),
            new Palette("Sunset", 0xFFFF9F0A, 0xF0241B12, 0xF01A130C, 0xFF352A1D, 0xFF453826),
    };

    private Theme() {}

    public static String[] names() {
        String[] n = new String[ALL.length];
        for (int i = 0; i < ALL.length; i++) n[i] = ALL[i].name();
        return n;
    }

    public static Palette current() {
        var ui = ModuleManager.UI;
        int i = ui != null && ui.enabled ? ui.theme.index : 0;
        return ALL[Math.max(0, Math.min(ALL.length - 1, i))];
    }

    public static int accent() { return current().accent(); }
    public static int panel() { return current().panel(); }
    public static int sidebar() { return current().sidebar(); }
    public static int card() { return current().card(); }
    public static int hover() { return current().hover(); }

    /** Czcionka menu wybrana w UI Theme. */
    public static String menuFont() {
        var ui = ModuleManager.UI;
        return ui != null && ui.enabled ? ui.font.get() : "Default";
    }

    /** Czcionka Dynamic Island (domyslnie Arial Bold). */
    public static String islandFont() {
        var ui = ModuleManager.UI;
        return ui != null && ui.enabled ? ui.islandFont.get() : "Arial Bold";
    }
}
