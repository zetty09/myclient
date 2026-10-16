package com.myclient.modules;

public class NumberSetting extends Setting {
    public float value;
    public final float min, max, step;

    public NumberSetting(String name, float def, float min, float max, float step) {
        super(name);
        this.value = def;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public float toFloat() { return value; }

    @Override
    public void fromFloat(float v) {
        if (step > 0) v = Math.round((v - min) / step) * step + min;
        value = Math.max(min, Math.min(max, v));
    }

    public String display() {
        if (step >= 1f) return String.valueOf(Math.round(value));
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }
}
