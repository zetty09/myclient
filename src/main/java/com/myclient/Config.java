package com.myclient;

import com.google.gson.Gson;
import com.myclient.cosmetics.WingType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public static String wings = "NONE";
    public static boolean island = true;
    public static boolean music = false;

    private static class Data {
        String wings = "NONE";
        boolean island = true;
        boolean music = false;
    }

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("myclient.json");
    private static final Gson GSON = new Gson();

    public static WingType wing() {
        try { return WingType.valueOf(wings); } catch (Exception e) { return WingType.NONE; }
    }

    public static void load() {
        try {
            if (!Files.exists(FILE)) return;
            Data d = GSON.fromJson(Files.readString(FILE), Data.class);
            wings = d.wings; island = d.island; music = d.music;
        } catch (Exception ignored) {}
    }

    public static void save() {
        try {
            Data d = new Data();
            d.wings = wings; d.island = island; d.music = music;
            Files.writeString(FILE, GSON.toJson(d));
        } catch (Exception ignored) {}
    }
}
