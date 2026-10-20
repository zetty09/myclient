package com.myclient.modules;

/** Wlasny crosshair zastepujacy domyslny. */
public class CrosshairModule extends Module {
    public final ModeSetting style = mode("Style", 0, "Cross", "Dot", "Circle", "Square");
    public final ModeSetting color = mode("Color", 0, "White", "Blue", "Red", "Green", "Purple", "Yellow", "Rainbow");
    public final NumberSetting size = number("Size", 5, 1, 20, 1);
    public final NumberSetting gap = number("Gap", 3, 0, 12, 1);
    public final NumberSetting thickness = number("Thickness", 1, 1, 4, 1);
    public final BoolSetting dot = bool("Center dot", false);
    public final BoolSetting outline = bool("Outline", true);
    public final BoolSetting dynamic = bool("Dynamic", true);

    public CrosshairModule() {
        super("Custom Crosshair", "Replaces the default crosshair", false, Category.VISUAL);
    }
}
