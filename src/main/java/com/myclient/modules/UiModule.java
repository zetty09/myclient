package com.myclient.modules;

/** Motyw otwierania i zamykania menu klienta. */
public class UiModule extends Module {
    public final ModeSetting open = mode("Open animation", 0, "Pop", "Zoom", "Fade", "Slide up", "Drop");
    public final ModeSetting close = mode("Close animation", 0, "Instant", "Fade", "Shrink", "Glass break");
    public final NumberSetting speed = number("Animation speed", 1f, 0.5f, 2f, 0.1f);
    public final NumberSetting glassTime = number("Glass stays (s)", 5f, 1f, 30f, 1f);
    public final ModeSetting cracks = mode("Glass cracks", 1, "Few", "Normal", "Many");
    public final ModeSetting tint = mode("Glass tint", 0, "Clear", "Blue", "Purple", "Red", "Green", "Yellow");
    public final BoolSetting sound = bool("Glass sound", true);

    public UiModule() {
        super("UI Theme", "Menu open / close animation (incl. breaking glass)", true);
    }
}
