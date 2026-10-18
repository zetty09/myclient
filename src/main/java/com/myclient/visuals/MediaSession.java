package com.myclient.visuals;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Odczyt tego, co gra na komputerze (Spotify, Apple Music itd.) przez systemowa sesje multimediow
 * Windowsa. Dziala przez staly proces PowerShell, ktory co chwile wypisuje stan odtwarzacza.
 * Tylko Windows 10/11.
 */
public final class MediaSession {
    public static final boolean SUPPORTED = System.getProperty("os.name", "").toLowerCase().contains("win");

    public static volatile String title = "";
    public static volatile String artist = "";
    public static volatile String status = "None";
    public static volatile String app = "";
    public static volatile boolean error = false;
    private static volatile double pos, end;
    private static volatile long stamp;

    private static Process proc;
    private static Path cmdFile;
    private static String lastKey = "";
    private static boolean firstTrack = true;

    private MediaSession() {}

    public static boolean running() { return proc != null && proc.isAlive(); }

    public static synchronized void start() {
        if (!SUPPORTED || running()) return;
        try {
            Path dir = FabricLoader.getInstance().getConfigDir().resolve("myclient");
            Files.createDirectories(dir);
            Path script = dir.resolve("media.ps1");
            cmdFile = dir.resolve("media.cmd");
            try (InputStream in = MediaSession.class.getResourceAsStream("/assets/myclient/media.ps1")) {
                if (in == null) { error = true; return; }
                Files.copy(in, script, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.deleteIfExists(cmdFile);
            error = false;
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive",
                    "-ExecutionPolicy", "Bypass", "-File", script.toString(), cmdFile.toString());
            pb.redirectErrorStream(true);
            Process p = pb.start();
            proc = p;
            Thread t = new Thread(() -> read(p), "japko-media");
            t.setDaemon(true);
            t.start();
            Runtime.getRuntime().addShutdownHook(new Thread(MediaSession::stop));
        } catch (Exception e) {
            error = true;
        }
    }

    public static synchronized void stop() {
        if (proc != null) proc.destroy();
        proc = null;
        title = artist = app = "";
        status = "None";
    }

    private static void read(Process p) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith("ERR")) { error = true; continue; }
                String[] f = line.split("\t", -1);
                if (f.length < 6) continue;
                error = false;
                title = f[0];
                artist = f[1];
                status = f[2];
                pos = num(f[3]);
                end = num(f[4]);
                app = friendly(f[5]);
                stamp = System.currentTimeMillis();
            }
        } catch (Exception ignored) {
        }
    }

    private static double num(String s) {
        try { return Double.parseDouble(s.trim().replace(',', '.')); } catch (Exception e) { return 0; }
    }

    private static String friendly(String id) {
        String l = id.toLowerCase();
        if (l.contains("spotify")) return "Spotify";
        if (l.contains("applemusic") || l.contains("itunes") || l.contains("apple")) return "Apple Music";
        if (l.contains("chrome")) return "Chrome";
        if (l.contains("firefox")) return "Firefox";
        if (l.contains("edge")) return "Edge";
        return id.replace(".exe", "");
    }

    public static boolean hasTrack() { return !title.isEmpty() && !status.equals("None"); }

    public static boolean isPlaying() { return status.equals("Playing"); }

    /** Pozycja w sekundach z interpolacja miedzy odczytami. */
    public static double position() {
        double p = pos;
        if (isPlaying()) p += (System.currentTimeMillis() - stamp) / 1000.0;
        return end > 0 ? Math.min(p, end) : p;
    }

    public static double duration() { return end; }

    public static void command(String cmd) {
        if (cmdFile == null || !running()) return;
        try { Files.writeString(cmdFile, cmd, StandardCharsets.UTF_8); } catch (Exception ignored) {}
    }

    public static void toggle() { command("toggle"); }
    public static void next() { command("next"); }
    public static void prev() { command("prev"); }

    /** Co tick: wykrywa zmiane utworu i daje powiadomienie w Dynamic Island. */
    public static void tick(MinecraftClient c, boolean notify) {
        if (!running()) { lastKey = ""; firstTrack = true; return; }
        String key = title + "|" + artist;
        if (!hasTrack()) return;
        if (!key.equals(lastKey)) {
            lastKey = key;
            if (!firstTrack && notify) {
                DynamicIsland.push("Now playing", artist.isEmpty() ? title : title + " - " + artist, 2800);
            }
            firstTrack = false;
        }
    }
}
