package com.myclient.modules;

import com.myclient.visuals.DynamicIsland;
import net.minecraft.client.MinecraftClient;

public abstract class Module {
    public final String name;
    public final String description;
    public final boolean defaultOn;
    public boolean enabled;
    /** Kod klawisza GLFW, -1 = brak bindu. */
    public int key = -1;

    protected Module(String name, String description, boolean defaultOn) {
        this.name = name;
        this.description = description;
        this.defaultOn = defaultOn;
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick(MinecraftClient c) {}

    public void toggle() { setEnabled(!enabled, true); }

    public void setEnabled(boolean on, boolean notify) {
        if (on == enabled) return;
        enabled = on;
        if (on) onEnable(); else onDisable();
        if (notify) DynamicIsland.push(name, on ? "Enabled" : "Disabled", 1400);
    }
}
