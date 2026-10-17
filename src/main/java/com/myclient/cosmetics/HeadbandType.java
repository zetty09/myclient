package com.myclient.cosmetics;

import net.minecraft.util.math.MathHelper;

public enum HeadbandType {
    NONE("None", 0),
    CRIMSON("Crimson", 0xFFD83A3A),
    MIDNIGHT("Midnight", 0xFF2B2B3C),
    SNOW("Snow", 0xFFF2F2F2),
    VIOLET("Violet", 0xFF7C5CFF),
    GOLD("Gold", 0xFFF2B632),
    OCEAN("Ocean", 0xFF2EC4E6),
    RAINBOW("Rainbow", 0);

    public final String label;
    private final int color;

    HeadbandType(String label, int color) {
        this.label = label;
        this.color = color;
    }

    /** Kolor ARGB; 0 dla NONE. Tecza jest animowana. */
    public int color() {
        if (this == NONE) return 0;
        if (this == RAINBOW) {
            float hue = (System.currentTimeMillis() % 4000L) / 4000f;
            return 0xFF000000 | MathHelper.hsvToRgb(hue, 0.75f, 1f);
        }
        return color;
    }
}
