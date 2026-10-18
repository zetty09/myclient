package com.myclient.visuals;

import java.util.List;

/** Wszystkie elementy HUD, ktore mozna przesuwac. Kolejne visuale dopisujemy tutaj. */
public final class HudElements {
    public static final InfoHud INFO = new InfoHud();
    public static final MediaHud MEDIA = new MediaHud();
    public static final List<HudElement> ALL = List.of(INFO, MEDIA);

    private HudElements() {}
}
