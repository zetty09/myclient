package com.myclient.mixin;

import com.myclient.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Pokazuje nick wlasnego gracza w trzeciej osobie (F5). */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void aether$ownLabel(LivingEntity entity, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (entity == mc.player
                && ModuleManager.NAMETAG != null && ModuleManager.NAMETAG.enabled
                && !mc.options.getPerspective().isFirstPerson()
                && MinecraftClient.isHudEnabled()) {
            cir.setReturnValue(true);
        }
    }
}
