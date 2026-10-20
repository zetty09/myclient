package com.myclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.myclient.cosmetics.WingType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static class ModState {
        public boolean enabled;
        public int key = -1;
        public Map<String, Float> settings = new HashMap<>();
    }

    public static class Data {
        public String wings = "NONE";
        public String headband = "NONE";
        public String fists = "NONE";
        /** Nazwa pliku .png z folderu config/myclient/capes, pusty = bez wlasnej peleryny. */
        public String cape = "";
        /** Tekst w Dynamic Island. Puste = nick z konta. */
        public String islandName = "jape3kk_dev";
        public Map<String, ModState> modules = new HashMap<>();
        /** Pozycje elementow HUD jako ulamki ekranu {x, y}. */
        public Map<String, float[]> hud = new HashMap<>();
    }

    public static Data data = new Data();

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("myclient.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static WingType wing() {
        try { return WingType.valueOf(data.wings); } catch (Exception e) { return WingType.NONE; }
    }

    public static com.myclient.cosmetics.HeadbandType headband() {
        try { return com.myclient.cosmetics.HeadbandType.valueOf(data.headband); } catch (Exception e) { return com.myclient.cosmetics.HeadbandType.NONE; }
    }

    public static com.myclient.cosmetics.FistType fists() {
        try { return com.myclient.cosmetics.FistType.valueOf(data.fists); } catch (Exception e) { return com.myclient.cosmetics.FistType.NONE; }
    }

    public static void load() {
        try {
            if (!Files.exists(FILE)) return;
            Data d = GSON.fromJson(Files.readString(FILE), Data.class);
            if (d == null) return;
            if (d.wings == null) d.wings = "NONE";
            if (d.headband == null) d.headband = "NONE";
            if (d.fists == null) d.fists = "NONE";
            if (d.cape == null) d.cape = "";
            if (d.islandName == null) d.islandName = "jape3kk_dev";
            if (d.modules == null) d.modules = new HashMap<>();
            if (d.hud == null) d.hud = new HashMap<>();
            data = d;
        } catch (Exception ignored) {}
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(data));
        } catch (Exception ignored) {}
    }
}
