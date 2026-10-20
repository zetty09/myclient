package com.myclient.modules;

import com.myclient.visuals.MediaSession;

/** Karta z muzyka grajaca na komputerze (Spotify, Apple Music, ...). Tylko Windows. */
public class MusicModule extends Module {
    public final BoolSetting controls = bool("Controls", true);
    public final BoolSetting progress = bool("Progress bar", true);
    public final BoolSetting notify = bool("Track notifications", true);
    public final BoolSetting islandMusic = bool("Show in Dynamic Island", true);
    public final BoolSetting cover = bool("Album cover", true);
    public final BoolSetting lyrics = bool("Lyrics (lrclib.net)", true);
    public final NumberSetting lyricsOffset = number("Lyrics offset (s)", 0f, -3f, 3f, 0.1f);
    public final NumberSetting opacity = number("Opacity", 85, 0, 100, 5);
    public final NumberSetting scale = number("Scale", 1.0f, 0.5f, 2.0f, 0.1f);

    public MusicModule() {
        super("Music Player", "Shows what plays on your PC (Spotify, Apple Music) - Windows only", false, Category.VISUAL);
    }

    @Override
    public void onEnable() { MediaSession.start(); }

    @Override
    public void onDisable() { MediaSession.stop(); }
}
