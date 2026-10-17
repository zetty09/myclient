package com.myclient.cosmetics;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/** Pomocnik: rysuje teksturowany prostopadloscian w przestrzeni modelu (y rosnie w dol). */
public final class CosmeticDraw {
    public static final Identifier BAND = Identifier.of("myclient", "textures/bands/band.png");
    public static final Identifier FIST = Identifier.of("myclient", "textures/bands/fist.png");

    private CosmeticDraw() {}

    public static void box(MatrixStack.Entry e, VertexConsumer vc,
                           float x0, float y0, float z0, float x1, float y1, float z1,
                           int color, int light) {
        // przod (-z)
        face(e, vc, color, light, 0, 0, -1, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0);
        // tyl (+z)
        face(e, vc, color, light, 0, 0, 1, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1);
        // lewo
        face(e, vc, color, light, -1, 0, 0, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1);
        // prawo
        face(e, vc, color, light, 1, 0, 0, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0);
        // gora
        face(e, vc, color, light, 0, -1, 0, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0);
        // dol
        face(e, vc, color, light, 0, 1, 0, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1);
    }

    private static void face(MatrixStack.Entry e, VertexConsumer vc, int color, int light,
                             float nx, float ny, float nz,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        v(e, vc, color, light, nx, ny, nz, ax, ay, az, 0f, 0f);
        v(e, vc, color, light, nx, ny, nz, bx, by, bz, 1f, 0f);
        v(e, vc, color, light, nx, ny, nz, cx, cy, cz, 1f, 1f);
        v(e, vc, color, light, nx, ny, nz, dx, dy, dz, 0f, 1f);
    }

    private static void v(MatrixStack.Entry e, VertexConsumer vc, int color, int light,
                          float nx, float ny, float nz, float x, float y, float z, float u, float t) {
        vc.vertex(e, x, y, z).color(color).texture(u, t).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, nx, ny, nz);
    }
}
