package com.myclient.modules;

/** Jeden widget z FPS, pingiem, TPS i koordynatami. */
public class InfoModule extends Module {
    public final BoolSetting fps = bool("FPS", true);
    public final BoolSetting ping = bool("Ping", true);
    public final BoolSetting tps = bool("TPS", true);
    public final BoolSetting coords = bool("Coordinates", false);
    public final BoolSetting horizontal = bool("Horizontal layout", false);
    public final BoolSetting background = bool("Background", true);
    public final NumberSetting opacity = number("Opacity", 70, 0, 100, 5);
    public final NumberSetting scale = number("Scale", 1.0f, 0.5f, 2.0f, 0.1f);

    public InfoModule() {
        super("Information", "FPS, ping, TPS and more in one widget", true, Category.VISUAL);
    }
}
