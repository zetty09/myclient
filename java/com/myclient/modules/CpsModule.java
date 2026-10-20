package com.myclient.modules;

/** Licznik klikniec na sekunde. */
public class CpsModule extends Module {
    public final BoolSetting right = bool("Right click", true);
    public final BoolSetting background = bool("Background", true);
    public final NumberSetting opacity = number("Opacity", 70, 0, 100, 5);
    public final NumberSetting scale = number("Scale", 1.0f, 0.5f, 2.0f, 0.1f);

    public CpsModule() {
        super("CPS", "Clicks per second counter", false, Category.VISUAL);
    }
}
