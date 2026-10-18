package com.myclient.modules;

/** Pokazuje wcisniete klawisze WASD, spacje i przyciski myszy. */
public class KeystrokesModule extends Module {
    public final BoolSetting mouse = bool("Mouse buttons", true);
    public final BoolSetting cps = bool("Show CPS", true);
    public final BoolSetting space = bool("Space bar", true);
    public final NumberSetting opacity = number("Opacity", 55, 0, 100, 5);
    public final NumberSetting scale = number("Scale", 1.0f, 0.5f, 2.0f, 0.1f);

    public KeystrokesModule() {
        super("Keystrokes", "Shows WASD, space and mouse buttons", false, Category.VISUAL);
    }
}
