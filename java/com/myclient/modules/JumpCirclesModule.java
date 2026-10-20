package com.myclient.modules;

/** Kolo rozchodzace sie po ziemi w miejscu, z ktorego skoczyles. */
public class JumpCirclesModule extends Module {
    public final ModeSetting color = mode("Color", 0, "Blue", "Purple", "Red", "Green", "Yellow", "White", "Rainbow");
    public final NumberSetting radius = number("Radius", 2f, 1f, 4f, 0.5f);
    public final NumberSetting duration = number("Duration", 0.9f, 0.4f, 2f, 0.1f);
    public final NumberSetting thickness = number("Thickness", 2, 1, 4, 1);

    public JumpCirclesModule() {
        super("Jump Circles", "A circle spreads from your feet when you jump", false, Category.VISUAL);
    }
}
