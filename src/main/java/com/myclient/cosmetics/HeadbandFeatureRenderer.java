package com.myclient.cosmetics;

import com.myclient.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

/** Opaska na glowie (tylko lokalny gracz): obreczy wokol czola + wezel z dwoma koncami z tylu. */
public class HeadbandFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {

    public HeadbandFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        HeadbandType type = Config.headband();
        if (type == HeadbandType.NONE) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || state.id != mc.player.getId() || state.invisible) return;

        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(CosmeticDraw.BAND));
        int color = type.color();

        matrices.push();
        getContextModel().head.rotate(matrices);
        MatrixStack.Entry e = matrices.peek();

        float h = 0.265f, y0 = -0.31f, y1 = -0.19f;
        CosmeticDraw.box(e, vc, -h, y0, -h, h, y1, h, color, light);
        // wezel
        CosmeticDraw.box(e, vc, -0.045f, y0, h, 0.045f, y1 + 0.01f, h + 0.045f, color, light);
        // dwa konce
        CosmeticDraw.box(e, vc, -0.105f, y0 + 0.02f, h + 0.02f, -0.045f, y1 + 0.20f, h + 0.045f, color, light);
        CosmeticDraw.box(e, vc, 0.045f, y0 + 0.02f, h + 0.02f, 0.105f, y1 + 0.22f, h + 0.045f, color, light);

        matrices.pop();
    }
}
