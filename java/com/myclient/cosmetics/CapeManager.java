package com.myclient.cosmetics;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.myclient.Config;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

/**
 * Wlasne peleryny: pliki .png w config/myclient/capes albo pobranie po nicku gracza
 * (publiczne API Mojanga). Peleryna jest widoczna tylko u Ciebie.
 */
public final class CapeManager {
    /** Komunikat do pokazania w menu. */
    public static volatile String status = "";

    private static String loadedName;
    private static long loadedStamp;
    private static Identifier loadedId;
    private static long lastCheck;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private CapeManager() {}

    public static Path dir() {
        Path p = FabricLoader.getInstance().getConfigDir().resolve("myclient").resolve("capes");
        try { Files.createDirectories(p); } catch (Exception ignored) {}
        return p;
    }

    public static List<String> list() {
        try (Stream<Path> s = Files.list(dir())) {
            return s.map(p -> p.getFileName().toString())
                    .filter(n -> n.toLowerCase().endsWith(".png"))
                    .sorted()
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Tekstura wybranej peleryny albo null. Wolane z renderu, wiec tanie (sprawdza plik co 1 s). */
    public static Identifier texture() {
        String n = Config.data.cape;
        if (n == null || n.isEmpty()) return null;
        if (n.contains("/") || n.contains("\\") || n.contains("..")) return null;

        long now = System.currentTimeMillis();
        if (n.equals(loadedName) && now - lastCheck < 1000) return loadedId;
        lastCheck = now;

        Path f = dir().resolve(n);
        if (!Files.isRegularFile(f)) {
            loadedName = n;
            loadedId = null;
            return null;
        }
        long stamp = f.toFile().lastModified();
        if (n.equals(loadedName) && stamp == loadedStamp) return loadedId;

        try (InputStream in = Files.newInputStream(f)) {
            NativeImage img = NativeImage.read(in);
            MinecraftClient mc = MinecraftClient.getInstance();
            Identifier id = Identifier.of("myclient",
                    "cape/" + n.toLowerCase().replaceAll("[^a-z0-9._-]", "_") + "_" + stamp);
            mc.getTextureManager().registerTexture(id, new NativeImageBackedTexture(img));
            if (loadedId != null) mc.getTextureManager().destroyTexture(loadedId);
            loadedName = n;
            loadedStamp = stamp;
            loadedId = id;
            return id;
        } catch (Exception e) {
            loadedName = n;
            loadedStamp = stamp;
            loadedId = null;
            status = "Could not read cape file: " + n;
            return null;
        }
    }

    /** Pobiera peleryne gracza o danym nicku, zapisuje do folderu i wybiera ja. */
    public static void fetch(String name) {
        if (name == null || !name.matches("[A-Za-z0-9_]{3,16}")) {
            status = "Invalid nickname";
            return;
        }
        status = "Downloading...";
        Thread t = new Thread(() -> {
            try {
                String id = null;
                try {
                    id = JsonParser.parseString(get("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + name))
                            .getAsJsonObject().get("id").getAsString();
                } catch (Exception ignored) {}
                if (id == null) {
                    id = JsonParser.parseString(get("https://api.mojang.com/users/profiles/minecraft/" + name))
                            .getAsJsonObject().get("id").getAsString();
                }

                JsonObject profile = JsonParser.parseString(get("https://sessionserver.mojang.com/session/minecraft/profile/" + id))
                        .getAsJsonObject();
                String capeUrl = null;
                for (JsonElement el : profile.getAsJsonArray("properties")) {
                    JsonObject p = el.getAsJsonObject();
                    if (!"textures".equals(p.get("name").getAsString())) continue;
                    String json = new String(Base64.getDecoder().decode(p.get("value").getAsString()), StandardCharsets.UTF_8);
                    JsonObject tex = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("textures");
                    if (tex != null && tex.has("CAPE")) capeUrl = tex.getAsJsonObject("CAPE").get("url").getAsString();
                }
                if (capeUrl == null) {
                    status = name + " has no cape";
                    return;
                }

                HttpResponse<byte[]> res = HTTP.send(
                        HttpRequest.newBuilder(URI.create(capeUrl.replace("http://", "https://")))
                                .timeout(Duration.ofSeconds(10)).GET().build(),
                        HttpResponse.BodyHandlers.ofByteArray());
                if (res.statusCode() != 200) throw new IllegalStateException("HTTP " + res.statusCode());

                String file = name.toLowerCase() + ".png";
                Files.write(dir().resolve(file), res.body());
                Config.data.cape = file;
                Config.save();
                status = "Cape of " + name + " selected";
            } catch (Exception e) {
                status = "Download failed (check nickname / internet)";
            }
        }, "myclient-cape-fetch");
        t.setDaemon(true);
        t.start();
    }

    private static String get(String url) throws Exception {
        HttpResponse<String> res = HTTP.send(
                HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new IllegalStateException("HTTP " + res.statusCode());
        return res.body();
    }
}
