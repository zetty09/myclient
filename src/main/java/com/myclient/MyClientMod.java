package com.myclient;

import com.myclient.cosmetics.FistsFeatureRenderer;
import com.myclient.cosmetics.HeadbandFeatureRenderer;
import com.myclient.cosmetics.WingsFeatureRenderer;
import com.myclient.modules.ModuleManager;
import com.myclient.ui.ChatMenu;
import com.myclient.ui.HudEditorScreen;
import com.myclient.ui.MainMenuScreen;
import com.myclient.visuals.DynamicIsland;
import com.myclient.visuals.HudElements;
import com.myclient.visuals.MusicPlayer;
import com.myclient.visuals.TpsTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MyClientMod implements ClientModInitializer {
    public static KeyBinding MENU_KEY;

    @Override
    public void onInitializeClient() {
        Config.load();
        ModuleManager.init();

        MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.myclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.myclient"));

        // skrzydla na modelu gracza
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof PlayerEntityRenderer pr) {
                helper.register(new WingsFeatureRenderer(pr));
                helper.register(new HeadbandFeatureRenderer(pr));
                helper.register(new FistsFeatureRenderer(pr));
            }
        });

        // menu na klawisz + logika modulow i bindy
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (MENU_KEY.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new MainMenuScreen());
            }
            ModuleManager.tick(client);
            MusicPlayer.tick(client);
        });

        // wizualy na HUD
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient c = MinecraftClient.getInstance();
            if (c.options.hudHidden) return;
            if (ModuleManager.ISLAND.enabled) DynamicIsland.render(ctx, c);
            // w edytorze HUD elementy rysuje sam edytor
            boolean editing = c.currentScreen instanceof HudEditorScreen;
            if (!editing) {
                if (HudElements.INFO.enabled()) HudElements.INFO.render(ctx, c);
                if (ModuleManager.MUSIC.enabled) MusicPlayer.render(ctx, c);
            }
        });

        // prawy klik w czacie -> lista visuali
        ChatMenu.register();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            TpsTracker.reset();
            var entry = client.getCurrentServerEntry();
            DynamicIsland.push("Connected", entry != null ? entry.address : "Singleplayer", 2500);
        });
    }
}
