package com.myclient.modules;

/** Ustawienie modulu (zapisywane jako liczba w configu). */
public abstract class Setting {
    public final String name;
    /** Czy ustawienie ma byc pokazane w oknie ustawien modulu. */
    public java.util.function.BooleanSupplier visibleWhen = () -> true;

    protected Setting(String name) {
        this.name = name;
    }

    public abstract float toFloat();

    public abstract void fromFloat(float v);
}
