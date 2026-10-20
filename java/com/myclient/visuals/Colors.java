package com.myclient.visuals;

import net.minecraft.util.math.MathHelper;

/** Nazwane kolory uzywane w ustawieniach (styl iOS). */
public final class Colors {
    private Colors() {}

    public static int rainbow() {
        float hue = (System.currentTimeMillis() % 3000L) / 3000f;
        return 0xFF000000 | MathHelper.hsvToRgb(hue, 0.7f, 1f);
    }

    public static int pick(String name) {
        return switch (name) {
            case "Red" -> 0xFFFF453A;
            case "Green" -> 0xFF30D158;
            case "Purple" -> 0xFFBF5AF2;
            case "Yellow" -> 0xFFFFD60A;
            case "White" -> 0xFFFFFFFF;
            case "Rainbow" -> rainbow();
            default -> 0xFF0A84FF;
        };
    }
}
