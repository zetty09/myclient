package com.myclient.mixin;

import com.myclient.modules.ModuleManager;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Zoom: kolko myszy zmienia przyblizenie zamiast przewijac pasek przedmiotow. */
@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true, require = 0)
    private void japko$scroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        var z = ModuleManager.ZOOM;
        if (z != null && z.enabled && z.scroll.value) {
            z.onScroll(vertical);
            ci.cancel();
        }
    }
}
