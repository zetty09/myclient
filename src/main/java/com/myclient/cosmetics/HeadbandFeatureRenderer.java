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
        int dark = CosmeticDraw.shade(color, 0.72f);
        int soft = CosmeticDraw.shade(color, 0.88f);
        float o = h + 0.008f;
        CosmeticDraw.box(e, vc, -h, y0, -h, h, y1, h, color, light);
        // wypustki (gora i dol) - efekt zszytego brzegu
        CosmeticDraw.box(e, vc, -o, y0 - 0.004f, -o, o, y0 + 0.016f, o, dark, light);
        CosmeticDraw.box(e, vc, -o, y1 - 0.016f, -o, o, y1 + 0.004f, o, dark, light);
        // wezel
        CosmeticDraw.box(e, vc, -0.06f, y0 - 0.004f, h, 0.06f, y1 + 0.014f, h + 0.05f, dark, light);
        CosmeticDraw.box(e, vc, -0.045f, y0, h + 0.002f, 0.045f, y1 + 0.01f, h + 0.058f, color, light);
        // dwa konce (rozchylone, o roznej dlugosci)
        CosmeticDraw.box(e, vc, -0.115f, y0 + 0.03f, h + 0.02f, -0.04f, y1 + 0.24f, h + 0.04f, soft, light);
        CosmeticDraw.box(e, vc, 0.04f, y0 + 0.03f, h + 0.02f, 0.115f, y1 + 0.30f, h + 0.04f, soft, light);
        CosmeticDraw.box(e, vc, -0.115f, y1 + 0.2f, h + 0.019f, -0.04f, y1 + 0.24f, h + 0.041f, dark, light);
        CosmeticDraw.box(e, vc, 0.04f, y1 + 0.26f, h + 0.019f, 0.115f, y1 + 0.30f, h + 0.041f, dark, light);

        matrices.pop();
    }
}
