package com.myclient.mixin;

import com.myclient.modules.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Ukrywa domyslny crosshair, gdy wlaczony jest Custom Crosshair. */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
    private void aether$hideCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ModuleManager.CROSSHAIR != null && ModuleManager.CROSSHAIR.enabled) ci.cancel();
    }
}
