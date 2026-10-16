package com.myclient.visuals;

import com.myclient.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Element HUD, ktory mozna przesuwac w edytorze. Pozycja = ulamek ekranu. */
public abstract class HudElement {
    public final String id;
    public final String label;
    private final float defX, defY;
    /** Rozmiar z ostatniego renderu (px, po skali). */
    public int w = 60, h = 20;

    protected HudElement(String id, String label, float defX, float defY) {
        this.id = id;
        this.label = label;
        this.defX = defX;
        this.defY = defY;
    }

    public abstract boolean enabled();

    /** Musi ustawic w/h, a potem narysowac sie w x(sw), y(sh). */
    public abstract void render(DrawContext ctx, MinecraftClient c);

    private float[] pos() {
        float[] p = Config.data.hud.get(id);
        return p != null && p.length >= 2 ? p : new float[]{defX, defY};
    }

    public int x(int sw) { return Math.round(pos()[0] * Math.max(0, sw - w)); }

    public int y(int sh) { return Math.round(pos()[1] * Math.max(0, sh - h)); }

    public void setPos(int x, int y, int sw, int sh) {
        float fx = sw - w <= 0 ? 0f : Math.max(0f, Math.min(1f, x / (float) (sw - w)));
        float fy = sh - h <= 0 ? 0f : Math.max(0f, Math.min(1f, y / (float) (sh - h)));
        Config.data.hud.put(id, new float[]{fx, fy});
    }
}
