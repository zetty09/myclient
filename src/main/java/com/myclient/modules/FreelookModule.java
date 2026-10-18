package com.myclient.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import org.lwjgl.glfw.GLFW;

/** Freelook: trzymasz klawisz (domyslnie lewy Alt) i rozgladasz sie kamera bez obracania postaci. */
public class FreelookModule extends Module {
    public final BoolSetting thirdPerson = bool("Third person view", true);
    public final BoolSetting invertPitch = bool("Invert pitch", false);

    public float yaw, pitch;
    private Perspective prev;
    private boolean changed;

    public FreelookModule() {
        super("Freelook", "Hold a key (default: Left Alt) to look around without turning", false);
        hold = true;
        defaultKey = GLFW.GLFW_KEY_LEFT_ALT;
    }

    @Override
    public void onEnable() {
        MinecraftClient c = MinecraftClient.getInstance();
        if (c.player == null) return;
        yaw = c.player.getYaw();
        pitch = c.player.getPitch();
        changed = false;
        if (thirdPerson.value && c.options.getPerspective().isFirstPerson()) {
            prev = c.options.getPerspective();
            c.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            changed = true;
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient c = MinecraftClient.getInstance();
        if (changed && prev != null) c.options.setPerspective(prev);
        changed = false;
    }

    /** Ruch myszy -> obrot kamery (zamiast obrotu gracza). */
    public void look(double dx, double dy) {
        yaw += (float) dx * 0.15f;
        float dp = (float) dy * 0.15f * (invertPitch.value ? -1f : 1f);
        pitch = Math.max(-90f, Math.min(90f, pitch + dp));
    }
}
