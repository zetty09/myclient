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
    }

    public static class Data {
        public String wings = "NONE";
        /** Tekst w Dynamic Island. Puste = nick z konta. */
        public String islandName = "jape3kk_dev";
        public Map<String, ModState> modules = new HashMap<>();
    }

    public static Data data = new Data();

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("myclient.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static WingType wing() {
        try { return WingType.valueOf(data.wings); } catch (Exception e) { return WingType.NONE; }
    }

    public static void load() {
        try {
            if (!Files.exists(FILE)) return;
            Data d = GSON.fromJson(Files.readString(FILE), Data.class);
            if (d == null) return;
            if (d.wings == null) d.wings = "NONE";
            if (d.islandName == null) d.islandName = "jape3kk_dev";
            if (d.modules == null) d.modules = new HashMap<>();
            data = d;
        } catch (Exception ignored) {}
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(data));
        } catch (Exception ignored) {}
    }
}
