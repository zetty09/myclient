package com.myclient.mixin;

import com.myclient.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Freelook: gdy aktywny, ruch myszy obraca tylko kamere, nie gracza. */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true, require = 0)
    private void japko$freelook(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        var f = ModuleManager.FREELOOK;
        if (f != null && f.enabled && (Object) this == MinecraftClient.getInstance().player) {
            f.look(cursorDeltaX, cursorDeltaY);
            ci.cancel();
        }
    }
}
