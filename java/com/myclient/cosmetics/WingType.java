package com.myclient.cosmetics;

import net.minecraft.util.Identifier;

public enum WingType {
    NONE("None"),
    GOLDEN("Golden"),
    VERDANT("Verdant"),
    AMETHYST("Amethyst");

    public final String label;
    public final Identifier texture;

    WingType(String label) {
        this.label = label;
        this.texture = Identifier.of("myclient", "textures/wings/" + name().toLowerCase() + ".png");
    }
}
