package com.myclient.mixin;

import com.myclient.modules.ModuleManager;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Zoom: zmienia FOV zwracany przez getFov (dziala niezaleznie od tego, czy zwraca float czy double). */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void japko$zoom(CallbackInfoReturnable cir) {
        if (ModuleManager.ZOOM == null) return;
        float m = ModuleManager.ZOOM.multiplier();
        if (m >= 0.999f) return;
        Object v = cir.getReturnValue();
        double fov;
        if (v instanceof Float f) fov = f;
        else if (v instanceof Double d) fov = d;
        else return;
        double out = Math.toDegrees(2.0 * Math.atan(Math.tan(Math.toRadians(fov) / 2.0) * m));
        if (v instanceof Float) cir.setReturnValue((float) out);
        else cir.setReturnValue(out);
    }
}
