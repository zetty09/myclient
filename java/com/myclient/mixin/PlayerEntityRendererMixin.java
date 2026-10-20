package com.myclient.mixin;

import com.myclient.ClientInfo;
import com.myclient.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Dokleja logo klienta po lewej od nicku. */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
            at = @At("RETURN"), require = 0)
    private void aether$logo(AbstractClientPlayerEntity player, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (state.displayName == null) return;
        if (ModuleManager.NAMETAG == null || !ModuleManager.NAMETAG.enabled) return;
        if (player != MinecraftClient.getInstance().player) return;
        state.displayName = Text.literal(ClientInfo.LOGO + " ")
                .styled(s -> s.withColor(0x0A84FF))
                .append(state.displayName);
    }
}
