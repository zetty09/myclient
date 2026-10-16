package com.myclient.modules;

import com.myclient.visuals.DynamicIsland;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public enum Category { MODULE, VISUAL }

    public final String name;
    public final String description;
    public final boolean defaultOn;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();
    public boolean enabled;
    /** Kod klawisza GLFW, -1 = brak bindu. */
    public int key = -1;

    protected Module(String name, String description, boolean defaultOn) {
        this(name, description, defaultOn, Category.MODULE);
    }

    protected Module(String name, String description, boolean defaultOn, Category category) {
        this.name = name;
        this.description = description;
        this.defaultOn = defaultOn;
        this.category = category;
    }

    protected BoolSetting bool(String name, boolean def) {
        BoolSetting s = new BoolSetting(name, def);
        settings.add(s);
        return s;
    }

    protected NumberSetting number(String name, float def, float min, float max, float step) {
        NumberSetting s = new NumberSetting(name, def, min, max, step);
        settings.add(s);
        return s;
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
