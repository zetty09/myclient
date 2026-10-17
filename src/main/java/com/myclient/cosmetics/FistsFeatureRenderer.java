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

/** Rekawice bokserskie na dloniach (tylko lokalny gracz). */
public class FistsFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {

    public FistsFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        FistType type = Config.fists();
        if (type == FistType.NONE) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || state.id != mc.player.getId() || state.invisible) return;

        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(CosmeticDraw.FIST));
        int color = type.color();
        PlayerEntityModel model = getContextModel();

        // prawa reka: przekroj -3..1 px => -0.1875..0.0625; rekawica dookola z zapasem
        matrices.push();
        model.rightArm.rotate(matrices);
        CosmeticDraw.box(matrices.peek(), vc, -0.2275f, 0.33f, -0.165f, 0.1025f, 0.67f, 0.165f, color, light);
        matrices.pop();

        // lewa reka: przekroj -1..3 px
        matrices.push();
        model.leftArm.rotate(matrices);
        CosmeticDraw.box(matrices.peek(), vc, -0.1025f, 0.33f, -0.165f, 0.2275f, 0.67f, 0.165f, color, light);
        matrices.pop();
    }
}
