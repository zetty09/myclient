package com.myclient.modules;

/** Pancerz i trzymany przedmiot z wytrzymaloscia. */
public class ArmorModule extends Module {
    public final ModeSetting value = mode("Durability", 0, "Number", "Percent", "None");
    public final BoolSetting vertical = bool("Vertical layout", true);
    public final BoolSetting heldItem = bool("Held item", true);
    public final BoolSetting background = bool("Background", true);
    public final NumberSetting opacity = number("Opacity", 70, 0, 100, 5);
    public final NumberSetting scale = number("Scale", 1.0f, 0.5f, 2.0f, 0.1f);

    public ArmorModule() {
        super("Armor HUD", "Shows your armor and held item with durability", false, Category.VISUAL);
    }
}
