package com.myclient.visuals;

import com.myclient.modules.ModuleManager;
import com.myclient.modules.TargetEspModule;
import com.myclient.ui.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Znacznik na encji pod celownikiem. Rysowany na HUD: punkty swiata sa rzutowane na ekran
 * (kamera + FOV z ustawien), wiec nie trzeba nic podpinac pod render swiata.
 */
public final class TargetEsp {
    private static long lastTickNs = System.nanoTime();
    private static long lastNs = System.nanoTime();
    private static Entity shown;
    private static float fade = 0f;

    private TargetEsp() {}

    /** Wolane co tick - sluzy do przyblizenia partial ticks. */
    public static void onTick() {
        lastTickNs = System.nanoTime();
    }

    public static void render(DrawContext ctx, MinecraftClient c) {
        TargetEspModule m = ModuleManager.TARGET_ESP;
        long now = System.nanoTime();
        float dt = Math.min((now - lastNs) / 1_000_000_000f, 0.05f);
        lastNs = now;

        Entity t = c.targetedEntity;
        boolean valid = t instanceof LivingEntity le && t != c.player && le.isAlive()
                && (!m.playersOnly.value || t instanceof PlayerEntity);
        if (valid) shown = t;
        fade += ((valid ? 1f : 0f) - fade) * (1f - (float) Math.exp(-dt * 14f));
        if (shown == null || shown.isRemoved() || !(shown instanceof LivingEntity)) {
            shown = null;
            return;
        }
        if (fade < 0.03f) {
            if (!valid) shown = null;
            return;
        }

        LivingEntity e = (LivingEntity) shown;
        Camera cam = c.gameRenderer.getCamera();
        float fov = c.options.getFov().getValue();
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();

        float d = Math.min(1f, (now - lastTickNs) / 50_000_000f);
        double x = e.prevX + (e.getX() - e.prevX) * d;
        double y = e.prevY + (e.getY() - e.prevY) * d;
        double z = e.prevZ + (e.getZ() - e.prevZ) * d;
        float w = e.getWidth(), h = e.getHeight();

        int col;
        String cn = m.color.get();
        if (cn.equals("Health")) {
            col = Gfx.lerpColor(0xFFFF453A, 0xFF30D158, Math.max(0f, Math.min(1f, e.getHealth() / e.getMaxHealth())));
        } else {
            col = Colors.pick(cn);
        }
        float size = m.size.value;
        int th = Math.round(m.thickness.value);
        double time = now / 1_000_000_000.0;

        Gfx.alpha = fade;
        try {
            switch (m.shape.get()) {
                case "Box" -> drawBox(ctx, cam, fov, sw, sh, x, y, z, w, h, size, time, col, th);
                case "Diamond" -> drawDiamond(ctx, cam, fov, sw, sh, x, y + h, z, size, time, col, th);
                case "Arrow" -> drawArrow(ctx, cam, fov, sw, sh, x, y + h, z, size, time, col);
                default -> drawRing(ctx, cam, fov, sw, sh, x, y, z, w, h, size, time, col, th);
            }
        } finally {
            Gfx.alpha = 1f;
        }
    }

    // ---------- rzutowanie swiat -> ekran ----------

    private static boolean project(Camera cam, float fov, int sw, int sh, double wx, double wy, double wz, float[] out) {
        Vec3d cp = cam.getPos();
        Vector3f v = new Vector3f((float) (wx - cp.x), (float) (wy - cp.y), (float) (wz - cp.z));
        new Quaternionf(cam.getRotation()).conjugate().transform(v);
        if (v.z >= -0.05f) return false; // za kamera
        float t = (float) Math.tan(Math.toRadians(fov) / 2.0);
        float aspect = (float) sw / sh;
        float ndcX = (v.x / -v.z) / (t * aspect);
        float ndcY = (v.y / -v.z) / t;
        out[0] = (ndcX * 0.5f + 0.5f) * sw;
        out[1] = (0.5f - ndcY * 0.5f) * sh;
        return true;
    }

    // ---------- ksztalty ----------

    /** Pierscien "latajacy" gora-dol wzdluz ciala, z pozostawiajacym slad cieniem. */
    private static void drawRing(DrawContext ctx, Camera cam, float fov, int sw, int sh,
                                 double x, double y, double z, float w, float h, float size, double time, int col, int th) {
        double r = Math.max(0.35, w * 0.75) * size;
        float baseAlpha = Gfx.alpha;
        for (int k = 3; k >= 0; k--) {
            double phase = time * 2.2 - k * 0.22;
            double yy = y + (0.5 + 0.5 * Math.sin(phase)) * h;
            Gfx.alpha = baseAlpha * (k == 0 ? 1f : 0.34f / k);
            ringAt(ctx, cam, fov, sw, sh, x, yy, z, r, col, k == 0 ? th : Math.max(1, th - 1));
        }
        Gfx.alpha = baseAlpha;
    }

    private static void ringAt(DrawContext ctx, Camera cam, float fov, int sw, int sh,
                               double x, double y, double z, double r, int col, int th) {
        int n = 36;
        float[] a = new float[2], b = new float[2];
        boolean haveA = false;
        for (int i = 0; i <= n; i++) {
            double ang = i / (double) n * Math.PI * 2.0;
            boolean ok = project(cam, fov, sw, sh, x + Math.cos(ang) * r, y, z + Math.sin(ang) * r, b);
            if (ok && haveA) Gfx.line(ctx, a[0], a[1], b[0], b[1], col, th);
            if (ok) {
                a[0] = b[0];
                a[1] = b[1];
                haveA = true;
            } else {
                haveA = false;
            }
        }
    }

    /** Obracajacy sie szescian (szkielet) w srodku modelu przeciwnika. */
    private static void drawBox(DrawContext ctx, Camera cam, float fov, int sw, int sh,
                                double x, double y, double z, float w, float h, float size, double time, int col, int th) {
        double half = Math.max(0.22, w * 0.45) * size;
        double cy = y + h / 2.0 + 0.04 * Math.sin(time * 2.5);
        double rot = time * 1.6;
        double cs = Math.cos(rot), sn = Math.sin(rot);
        float[][] p = new float[8][2];
        for (int i = 0; i < 8; i++) {
            double lx = (i & 1) == 0 ? -half : half;
            double ly = (i & 2) == 0 ? -half : half;
            double lz = (i & 4) == 0 ? -half : half;
            double rx = lx * cs - lz * sn, rz = lx * sn + lz * cs;
            if (!project(cam, fov, sw, sh, x + rx, cy + ly, z + rz, p[i])) return;
        }
        int[][] edges = {{0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
        for (int[] e : edges) Gfx.line(ctx, p[e[0]][0], p[e[0]][1], p[e[1]][0], p[e[1]][1], col, th);
    }

    private static void drawDiamond(DrawContext ctx, Camera cam, float fov, int sw, int sh,
                                    double x, double headY, double z, float size, double time, int col, int th) {
        double cy = headY + (0.5 + 0.06 * Math.sin(time * 3.0)) * size;
        double r = 0.2 * size, hh = 0.32 * size, rot = time * 2.0;
        float[] top = new float[2], bot = new float[2];
        float[][] ring = new float[4][2];
        if (!project(cam, fov, sw, sh, x, cy + hh, z, top)) return;
        if (!project(cam, fov, sw, sh, x, cy - hh, z, bot)) return;
        for (int k = 0; k < 4; k++) {
            double a = rot + k * Math.PI / 2.0;
            if (!project(cam, fov, sw, sh, x + Math.cos(a) * r, cy, z + Math.sin(a) * r, ring[k])) return;
        }
        for (int k = 0; k < 4; k++) {
            float[] r0 = ring[k], r1 = ring[(k + 1) % 4];
            Gfx.line(ctx, top[0], top[1], r0[0], r0[1], col, th);
            Gfx.line(ctx, bot[0], bot[1], r0[0], r0[1], col, th);
            Gfx.line(ctx, r0[0], r0[1], r1[0], r1[1], col, th);
        }
    }

    private static void drawArrow(DrawContext ctx, Camera cam, float fov, int sw, int sh,
                                  double x, double headY, double z, float size, double time, int col) {
        double bob = 0.05 * Math.sin(time * 3.0);
        float[] tip = new float[2], ref = new float[2];
        if (!project(cam, fov, sw, sh, x, headY + 0.2 * size + bob, z, tip)) return;
        if (!project(cam, fov, sw, sh, x, headY + 0.2 * size + bob + 0.3, z, ref)) return;
        float ppb = Math.abs(ref[1] - tip[1]) / 0.3f; // pikseli na blok
        float half = Math.max(3f, 0.18f * ppb * size);
        float hgt = Math.max(6f, 0.5f * ppb * size);
        int rows = Math.round(hgt);
        for (int i = 0; i < rows; i++) {
            float wdt = half * (1f - i / (float) rows);
            int yy = Math.round(tip[1] - hgt + i);
            ctx.fill(Math.round(tip[0] - wdt), yy, Math.round(tip[0] + wdt) + 1, yy + 1, Gfx.a(col));
        }
    }
}
