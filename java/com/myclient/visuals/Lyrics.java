package com.myclient.visuals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Zsynchronizowane teksty piosenek z lrclib.net (darmowe, bez klucza). Pobierane w tle po zmianie utworu;
 * do serwisu trafia tylko wykonawca, tytul i dlugosc utworu.
 */
public final class Lyrics {
    private record Line(double t, String text) {}

    private static volatile List<Line> lines = List.of();
    private static volatile String forKey = "";
    public static volatile boolean loading;
    public static volatile boolean found;

    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(6)).build();
    private static final Pattern STAMP = Pattern.compile("\\[(\\d{1,3}):(\\d{2}(?:[.:]\\d{1,3})?)]");

    private Lyrics() {}

    public static void clear() {
        lines = List.of();
        forKey = "";
        found = false;
        loading = false;
    }

    /** Zleca pobranie tekstu dla utworu (jesli to nowy utwor). */
    public static void request(String artist, String title, double durationSec) {
        String key = artist + "|" + title;
        if (key.equals(forKey)) return;
        forKey = key;
        lines = List.of();
        found = false;
        loading = true;
        Thread t = new Thread(() -> {
            List<Line> result = List.of();
            try {
                result = fetch(artist, title, durationSec);
            } catch (Exception ignored) {
            }
            if (key.equals(forKey)) {
                lines = result;
                found = !result.isEmpty();
                loading = false;
            }
        }, "japko-lyrics");
        t.setDaemon(true);
        t.start();
    }

    private static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }

    private static String get(String url) throws Exception {
        HttpRequest rq = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8))
                .header("User-Agent", "JapkoClient/1.2.0 (Minecraft mod)").GET().build();
        HttpResponse<String> rs = HTTP.send(rq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return rs.statusCode() == 200 ? rs.body() : null;
    }

    private static List<Line> fetch(String artist, String title, double dur) throws Exception {
        String base = "track_name=" + enc(title) + (artist.isEmpty() ? "" : "&artist_name=" + enc(artist));
        String synced = null;

        String body = get("https://lrclib.net/api/get?" + base + (dur > 0 ? "&duration=" + Math.round(dur) : ""));
        synced = syncedOf(body);
        if (synced == null && dur > 0) synced = syncedOf(get("https://lrclib.net/api/get?" + base));
        if (synced == null) {
            String res = get("https://lrclib.net/api/search?" + base);
            if (res != null) {
                JsonArray arr = JsonParser.parseString(res).getAsJsonArray();
                for (JsonElement e : arr) {
                    String s = syncedOf(e.toString());
                    if (s != null) { synced = s; break; }
                }
            }
        }
        return synced == null ? List.of() : parse(synced);
    }

    private static String syncedOf(String json) {
        if (json == null) return null;
        try {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            JsonElement s = o.get("syncedLyrics");
            if (s != null && !s.isJsonNull() && !s.getAsString().isBlank()) return s.getAsString();
        } catch (Exception ignored) {
        }
        return null;
    }

    private static List<Line> parse(String lrc) {
        List<Line> out = new ArrayList<>();
        for (String raw : lrc.split("\\r?\\n")) {
            Matcher m = STAMP.matcher(raw);
            List<Double> times = new ArrayList<>();
            int end = 0;
            while (m.find()) {
                double min = Double.parseDouble(m.group(1));
                double sec = Double.parseDouble(m.group(2).replace(':', '.'));
                times.add(min * 60 + sec);
                end = m.end();
            }
            if (times.isEmpty()) continue;
            String text = raw.substring(end).trim();
            for (double t : times) out.add(new Line(t, text));
        }
        out.sort(Comparator.comparingDouble(Line::t));
        return out;
    }

    /** Aktualna linijka dla pozycji w sekundach; null jesli nie ma tekstu (lub jeszcze sie nie zaczal). */
    public static String current(double pos) {
        List<Line> l = lines;
        if (l.isEmpty()) return null;
        String cur = null;
        for (Line line : l) {
            if (line.t() <= pos) cur = line.text();
            else break;
        }
        if (cur == null) return null;
        return cur.isEmpty() ? "♪" : cur;
    }
}
