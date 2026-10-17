package com.myclient.cosmetics;

public enum FistType {
    NONE("None", 0),
    RED("Boxing Red", 0xFFD83A3A),
    BLUE("Boxing Blue", 0xFF2F6BFF),
    SHADOW("Shadow", 0xFF2B2B3C),
    GOLD("Gold", 0xFFF2B632),
    VIOLET("Violet", 0xFF7C5CFF),
    SNOW("Snow", 0xFFF2F2F2);

    public final String label;
    private final int color;

    FistType(String label, int color) {
        this.label = label;
        this.color = color;
    }

    /** Kolor ARGB; 0 dla NONE. */
    public int color() {
        return color;
    }
}
