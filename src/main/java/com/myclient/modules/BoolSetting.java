package com.myclient.modules;

public class BoolSetting extends Setting {
    public boolean value;

    public BoolSetting(String name, boolean def) {
        super(name);
        this.value = def;
    }

    @Override
    public float toFloat() { return value ? 1f : 0f; }

    @Override
    public void fromFloat(float v) { value = v >= 0.5f; }
}
