package com.myclient.visuals;

import java.util.List;

/** Wszystkie elementy HUD, ktore mozna przesuwac. Kolejne visuale dopisujemy tutaj. */
public final class HudElements {
    public static final InfoHud INFO = new InfoHud();
    public static final MediaHud MEDIA = new MediaHud();
    public static final KeystrokesHud KEYS = new KeystrokesHud();
    public static final CpsHud CPS = new CpsHud();
    public static final ArmorHud ARMOR = new ArmorHud();
    public static final List<HudElement> ALL = List.of(INFO, MEDIA, KEYS, CPS, ARMOR);

    private HudElements() {}
}
