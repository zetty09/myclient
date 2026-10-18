package com.myclient.mixin;

import com.myclient.modules.ModuleManager;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Freelook: podmienia obrot kamery zaraz po tym, jak gra ustawi go wedlug gracza (przed liczeniem pozycji). */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V",
                    shift = At.Shift.AFTER, ordinal = 0),
            require = 0)
    private void japko$freelook(CallbackInfo ci) {
        var f = ModuleManager.FREELOOK;
        if (f != null && f.enabled) setRotation(f.yaw, f.pitch);
    }
}
