package com.myclient.mixin;

import com.myclient.modules.ModuleManager;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Freelook: podmienia obrot kamery w samym setRotation(yaw, pitch). Dzieki temu nie trzeba
 * szukac konkretnego wywolania w Camera.update - dziala niezaleznie od szczegolow tej metody.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private float japko$freelookYaw(float yaw) {
        var f = ModuleManager.FREELOOK;
        return f != null && f.enabled ? f.yaw : yaw;
    }

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 1, require = 0)
    private float japko$freelookPitch(float pitch) {
        var f = ModuleManager.FREELOOK;
        return f != null && f.enabled ? f.pitch : pitch;
    }
}
