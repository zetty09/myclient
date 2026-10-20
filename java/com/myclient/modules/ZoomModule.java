package com.myclient.modules;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/** Zoom: trzymasz klawisz (domyslnie C), kolkiem myszy zmieniasz przyblizenie. */
public class ZoomModule extends Module {
    public final NumberSetting level = number("Zoom level", 4f, 1.5f, 10f, 0.5f);
    public final BoolSetting smooth = bool("Smooth animation", true);
    public final BoolSetting scroll = bool("Scroll to zoom", true);
    public final BoolSetting cinematic = bool("Cinematic camera", false);

    /** Mnoznik z kolka myszy (1 = brak). */
    public float extra = 1f;
    private float cur = 1f;
    private long last = System.nanoTime();
    private boolean prevSmooth;

    public ZoomModule() {
        super("Zoom", "Hold a key to zoom in (default: C). Scroll changes the zoom", false);
        hold = true;
        defaultKey = GLFW.GLFW_KEY_C;
    }

    @Override
    public void onEnable() {
        if (cinematic.value) {
            MinecraftClient c = MinecraftClient.getInstance();
            prevSmooth = c.options.smoothCameraEnabled;
            c.options.smoothCameraEnabled = true;
        }
    }

    @Override
    public void onDisable() {
        if (cinematic.value) MinecraftClient.getInstance().options.smoothCameraEnabled = prevSmooth;
    }

    /** Biezacy mnoznik widoku (1 = bez zoomu, mniej = blizej). Wolane co klatke. */
    public float multiplier() {
        long now = System.nanoTime();
        float dt = Math.min((now - last) / 1_000_000_000f, 0.1f);
        last = now;
        if (!enabled) extra = 1f;
        float target = enabled ? 1f / (level.value * extra) : 1f;
        cur = smooth.value ? cur + (target - cur) * (1f - (float) Math.exp(-dt * 16f)) : target;
        if (!enabled && Math.abs(cur - 1f) < 0.003f) cur = 1f;
        return cur;
    }

    public void onScroll(double vertical) {
        if (vertical == 0) return;
        extra *= vertical > 0 ? 1.15f : 1f / 1.15f;
        extra = Math.max(0.4f, Math.min(3f, extra));
    }
}
