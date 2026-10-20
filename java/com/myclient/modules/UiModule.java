package com.myclient.modules;

/** Motyw otwierania i zamykania menu klienta. */
public class UiModule extends Module {
    public final ModeSetting theme = mode("Theme", 0, com.myclient.ui.Theme.names());
    public final ModeSetting font = mode("Menu font", 0, com.myclient.ui.Fonts.NAMES);
    public final ModeSetting islandFont = mode("Island font", 2, com.myclient.ui.Fonts.NAMES);
    public final ModeSetting open = mode("Open animation", 0, "Pop", "Zoom", "Fade", "Slide up", "Drop");
    public final ModeSetting close = mode("Close animation", 0, "Instant", "Fade", "Shrink", "Glass break");
    public final NumberSetting speed = number("Animation speed", 1f, 0.5f, 2f, 0.1f);
    public final NumberSetting glassTime = number("Glass stays (s)", 5f, 1f, 30f, 1f);
    public final ModeSetting cracks = mode("Glass cracks", 1, "Few", "Normal", "Many");
    public final ModeSetting tint = mode("Glass tint", 0, "Clear", "Blue", "Purple", "Red", "Green", "Yellow");
    public final BoolSetting sound = bool("Glass sound", true);

    public UiModule() {
        super("UI Theme", "Theme, font and menu open / close animation (incl. breaking glass)", true);
        // ustawienia szkla widoczne tylko, gdy wybrano "Glass break"
        glassTime.visibleWhen = () -> close.index == 3;
        cracks.visibleWhen = () -> close.index == 3;
        tint.visibleWhen = () -> close.index == 3;
        sound.visibleWhen = () -> close.index == 3;
    }
}
