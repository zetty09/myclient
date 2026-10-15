package com.myclient.mixin;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

/** Pozwala policzyc aktywne bossbary, zeby Dynamic Island zjechal pod nie. */
@Mixin(BossBarHud.class)
public interface BossBarHudAccessor {
    @Accessor("bossBars")
    Map<UUID, ClientBossBar> aether$getBossBars();
}
