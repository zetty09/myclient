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

        int dark = CosmeticDraw.shade(color, 0.62f);
        int cuff = CosmeticDraw.shade(color, 1.18f);

        // prawa reka: przekroj -3..1 px => -0.1875..0.0625; rekawica dookola z zapasem
        matrices.push();
        model.rightArm.rotate(matrices);
        MatrixStack.Entry r = matrices.peek();
        CosmeticDraw.box(r, vc, -0.2275f, 0.42f, -0.165f, 0.1025f, 0.735f, 0.165f, color, light);       // korpus
        CosmeticDraw.box(r, vc, -0.2175f, 0.375f, -0.155f, 0.0925f, 0.425f, 0.155f, cuff, light);        // gorny brzeg
        CosmeticDraw.box(r, vc, -0.215f, 0.31f, -0.152f, 0.09f, 0.38f, 0.152f, dark, light);             // mankiet
        CosmeticDraw.box(r, vc, 0.085f, 0.46f, -0.13f, 0.145f, 0.60f, 0.03f, color, light);              // kciuk
        CosmeticDraw.box(r, vc, -0.2175f, 0.69f, -0.158f, 0.0925f, 0.74f, 0.158f, dark, light);          // dol
        matrices.pop();

        // lewa reka: przekroj -1..3 px
        matrices.push();
        model.leftArm.rotate(matrices);
        MatrixStack.Entry l = matrices.peek();
        CosmeticDraw.box(l, vc, -0.1025f, 0.42f, -0.165f, 0.2275f, 0.735f, 0.165f, color, light);
        CosmeticDraw.box(l, vc, -0.0925f, 0.375f, -0.155f, 0.2175f, 0.425f, 0.155f, cuff, light);
        CosmeticDraw.box(l, vc, -0.09f, 0.31f, -0.152f, 0.215f, 0.38f, 0.152f, dark, light);
        CosmeticDraw.box(l, vc, -0.145f, 0.46f, -0.13f, -0.085f, 0.60f, 0.03f, color, light);
        CosmeticDraw.box(l, vc, -0.0925f, 0.69f, -0.158f, 0.2175f, 0.74f, 0.158f, dark, light);
        matrices.pop();
    }
}
