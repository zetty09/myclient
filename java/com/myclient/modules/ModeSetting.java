package com.myclient.modules;

/** Ustawienie wyboru z listy opcji (klik przelacza na kolejna). */
public class ModeSetting extends Setting {
    public final String[] options;
    public int index;

    public ModeSetting(String name, int def, String... options) {
        super(name);
        this.options = options;
        this.index = Math.max(0, Math.min(def, options.length - 1));
    }

    public String get() { return options[index]; }

    public void next() { index = (index + 1) % options.length; }

    @Override
    public float toFloat() { return index; }

    @Override
    public void fromFloat(float v) { index = Math.max(0, Math.min(options.length - 1, Math.round(v))); }
}
