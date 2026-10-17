package com.myclient;

/** Nazwa i wyglad klienta. Zmien NAME, jesli chcesz inna nazwe. */
public final class ClientInfo {
    public static final String NAME = "Japko Client";
    public static final String VERSION = "1.2.0";
    /** Symbol obok nicku (unicode escape, zeby nie zalezal od kodowania pliku). */
    public static final String LOGO = "✦";
    /** Niebieski iOS. */
    public static final int ACCENT = 0xFF0A84FF;

    private ClientInfo() {}
}
