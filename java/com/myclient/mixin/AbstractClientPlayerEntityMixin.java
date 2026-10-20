package com.myclient.mixin;

import com.myclient.cosmetics.CapeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Podmienia peleryne lokalnego gracza na wybrana w menu (zastepuje obecna). */
@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true, require = 0)
    private void aether$cape(CallbackInfoReturnable<SkinTextures> cir) {
        if ((Object) this != MinecraftClient.getInstance().player) return;
        Identifier cape = CapeManager.texture();
        if (cape == null) return;
        SkinTextures s = cir.getReturnValue();
        cir.setReturnValue(new SkinTextures(s.texture(), s.textureUrl(), cape, cape, s.model(), s.secure()));
    }
}
