package com.myclient.cosmetics;

import com.myclient.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

/** Rysuje skrzydla na plecach lokalnego gracza (dwa teksturowane quady z animacja machania). */
public class WingsFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {

    public WingsFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        WingType type = Config.wing();
        if (type == WingType.NONE) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || state.id != mc.player.getId()) return; // tylko Ty

        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(type.texture));
        float t = (System.nanoTime() / 1_000_000_000f);
        float flap = (float) Math.sin(t * 2.4f) * 14f;

        for (int side = -1; side <= 1; side += 2) {
            matrices.push();
            // model-space: y rosnie w dol, tyl postaci to +z
            matrices.translate(side * 0.07f, 0.0f, 0.15f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-side * (32f + flap)));
            quad(matrices.peek(), vc, side, light);
            matrices.pop();
        }
    }

    private static void quad(MatrixStack.Entry e, VertexConsumer vc, int side, int light) {
        float w = 0.95f * side;      // na zewnatrz
        float top = -0.30f;           // nad ramionami
        float bottom = 0.78f;         // do pasa
        vertex(e, vc, 0, top, 0f, 0f, light);
        vertex(e, vc, w, top, 1f, 0f, light);
        vertex(e, vc, w, bottom, 1f, 1f, light);
        vertex(e, vc, 0, bottom, 0f, 1f, light);
    }

    private static void vertex(MatrixStack.Entry e, VertexConsumer vc, float x, float y, float u, float v, int light) {
        vc.vertex(e, x, y, 0f).color(-1).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0f, 0f, 1f);
    }
}
