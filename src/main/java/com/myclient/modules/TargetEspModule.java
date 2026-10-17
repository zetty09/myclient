package com.myclient.modules;

/** Znacznik na przeciwniku, na ktorego patrzysz (kilka ksztaltow). */
public class TargetEspModule extends Module {
    public final ModeSetting shape = mode("Shape", 0, "Ring", "Box", "Diamond", "Arrow");
    public final ModeSetting color = mode("Color", 0, "Blue", "Red", "Green", "Purple", "Yellow", "Rainbow", "Health");
    public final NumberSetting size = number("Size", 1.0f, 0.5f, 2.0f, 0.1f);
    public final NumberSetting thickness = number("Thickness", 2, 1, 4, 1);
    public final BoolSetting playersOnly = bool("Players only", false);

    public TargetEspModule() {
        super("Target ESP", "Marks the entity you are looking at with a shape", false);
    }
}
