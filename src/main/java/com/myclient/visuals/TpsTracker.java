package com.myclient.visuals;

/**
 * Szacuje TPS serwera po odstepach miedzy pakietami czasu swiata
 * (serwer wysyla je co 20 tickow).
 */
public final class TpsTracker {
    private static long last = -1;
    private static float tps = -1;

    private TpsTracker() {}

    public static void onTimeUpdate() {
        long now = System.nanoTime();
        if (last > 0) {
            float secs = (now - last) / 1_000_000_000f;
            if (secs > 0.05f) {
                float v = Math.min(20f, 20f / secs);
                tps = tps < 0 ? v : tps * 0.6f + v * 0.4f;
            }
        }
        last = now;
    }

    public static void reset() {
        last = -1;
        tps = -1;
    }

    /** -1 gdy brak danych. */
    public static float get() {
        if (last < 0 || System.nanoTime() - last > 6_000_000_000L) return -1;
        return tps;
    }
}
