package com.myclient.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Efekt "rozbitego szkla": panel menu peka od punktu uderzenia na kawalki, ktore zostaja na ekranie
 * przez ustawiony czas, a potem opadaja i znikaja. Rysowane na HUD przez bufor wierzcholkow.
 */
public final class GlassBreak {
    private static final class Shard {
        float[] x = new float[4], y = new float[4];
        float cx, cy;          // srodek ciezkosci
        float dirX, dirY;      // kierunek rozrzutu
        float burst;           // sila rozrzutu (px)
        float rot;             // obrot koncowy (rad)
        float fallAtMs;        // kiedy zaczyna spadac (ms od startu)
        float driftX;          // znoszenie w bok podczas spadania
        float alpha;           // bazowa przezroczystosc wypelnienia (0..1)
        boolean dead;
    }

    private static final List<Shard> SHARDS = new ArrayList<>();
    private static long startMs;
    private static int tint = 0xFFDCEBFF;

    private GlassBreak() {}

    public static boolean active() { return !SHARDS.isEmpty(); }

    public static void clear() { SHARDS.clear(); }

    /**
     * Tworzy pekniecie. (px,py,w,h) = prostokat szyby, (ix,iy) = punkt uderzenia,
     * lifetime = jak dlugo kawalki zostaja (s), cracks = 0 malo, 1 normalnie, 2 duzo.
     */
    public static void shatter(int px, int py, int w, int h, float ix, float iy, float lifetimeSec, int cracks, int tintColor) {
        SHARDS.clear();
        startMs = System.currentTimeMillis();
        tint = tintColor;
        Random rnd = new Random();
        int sectors = switch (cracks) { case 0 -> 9; case 2 -> 18; default -> 13; };
        float q = switch (cracks) { case 0 -> 2.1f; case 2 -> 1.55f; default -> 1.8f; };

        float far = 0;
        for (int cx : new int[]{px, px + w}) {
            for (int cy : new int[]{py, py + h}) far = Math.max(far, (float) Math.hypot(cx - ix, cy - iy));
        }
        far += 20;

        List<Float> radii = new ArrayList<>();
        float r = 14f;
        while (r < far) {
            radii.add(r);
            r *= q;
        }
        radii.add(far * 1.1f);

        int rings = radii.size();
        float[][] ax = new float[rings][sectors], ay = new float[rings][sectors];
        float[] base = new float[sectors];
        for (int j = 0; j < sectors; j++) base[j] = (float) (j * Math.PI * 2 / sectors + (rnd.nextFloat() - 0.5f) * 0.35f);
        for (int k = 0; k < rings; k++) {
            for (int j = 0; j < sectors; j++) {
                float rr = radii.get(k) * (0.88f + rnd.nextFloat() * 0.24f);
                float a = base[j] + (rnd.nextFloat() - 0.5f) * 0.12f * (k + 1);
                ax[k][j] = ix + (float) Math.cos(a) * rr;
                ay[k][j] = iy + (float) Math.sin(a) * rr;
            }
        }

        float lifeMs = lifetimeSec * 1000f;
        // trojkaty przy punkcie uderzenia + czworokaty miedzy pierscieniami
        for (int k = -1; k < rings - 1; k++) {
            for (int j = 0; j < sectors; j++) {
                int j2 = (j + 1) % sectors;
                Shard s = new Shard();
                if (k < 0) {
                    s.x[0] = ix; s.y[0] = iy;
                    s.x[1] = ax[0][j]; s.y[1] = ay[0][j];
                    s.x[2] = ax[0][j2]; s.y[2] = ay[0][j2];
                    s.x[3] = ix; s.y[3] = iy;
                } else {
                    s.x[0] = ax[k][j]; s.y[0] = ay[k][j];
                    s.x[1] = ax[k][j2]; s.y[1] = ay[k][j2];
                    s.x[2] = ax[k + 1][j2]; s.y[2] = ay[k + 1][j2];
                    s.x[3] = ax[k + 1][j]; s.y[3] = ay[k + 1][j];
                }
                // przytnij do szyby
                for (int v = 0; v < 4; v++) {
                    s.x[v] = Math.max(px, Math.min(px + w, s.x[v]));
                    s.y[v] = Math.max(py, Math.min(py + h, s.y[v]));
                }
                float cx = 0, cy = 0;
                for (int v = 0; v < 4; v++) { cx += s.x[v]; cy += s.y[v]; }
                s.cx = cx / 4f;
                s.cy = cy / 4f;
                if (s.cx <= px + 1 || s.cx >= px + w - 1 || s.cy <= py + 1 || s.cy >= py + h - 1) continue;

                float dx = s.cx - ix, dy = s.cy - iy;
                float dist = Math.max(1f, (float) Math.hypot(dx, dy));
                s.dirX = dx / dist;
                s.dirY = dy / dist;
                float rel = Math.min(1f, dist / far);
                s.burst = 2f + 9f * (1f - rel) + rnd.nextFloat() * 3f;
                s.rot = (rnd.nextFloat() - 0.5f) * 0.12f;
                s.alpha = 0.10f + rnd.nextFloat() * 0.09f;
                s.driftX = (rnd.nextFloat() - 0.5f) * 90f;

                boolean hole = k < 0 || (k == 0 && rnd.nextFloat() < 0.5f);
                if (hole) {
                    s.fallAtMs = 120 + rnd.nextFloat() * 400;                 // dziura wokol uderzenia odpada od razu
                } else if (rnd.nextFloat() < 0.1f) {
                    s.fallAtMs = 400 + rnd.nextFloat() * Math.min(lifeMs * 0.8f, 2500f); // pojedyncze kawalki odpadaja wczesniej
                } else {
                    s.fallAtMs = lifeMs + rnd.nextFloat() * 900f;               // reszta po zadanym czasie
                }
                SHARDS.add(s);
            }
        }
    }

    private static final float G = 1500f;      // px / s^2
    private static final float FADE_MS = 700f;

    public static void render(DrawContext ctx, MinecraftClient c) {
        if (SHARDS.isEmpty()) return;
        float t = System.currentTimeMillis() - startMs;
        float burstE = 1f - (float) Math.pow(1f - Math.min(1f, t / 260f), 3); // rozrzut: szybki start, potem zastyga

        VertexConsumer vc = ctx.getVertexConsumers().getBuffer(RenderLayer.getGui());
        Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
        boolean any = false;

        // lekki blask w miejscu uderzenia tuz po pekniecieciu
        for (Shard s : SHARDS) {
            if (s.dead) continue;
            float ft = t - s.fallAtMs;
            float fall = 0f, fade = 1f;
            if (ft > 0) {
                float sec = ft / 1000f;
                fall = 0.5f * G * sec * sec;
                fade = 1f - Math.min(1f, ft / FADE_MS);
                if (fade <= 0f) { s.dead = true; continue; }
            }
            any = true;

            float off = s.burst * burstE;
            float ang = s.rot * burstE + (ft > 0 ? s.rot * 4f * (ft / 1000f) : 0f);
            float cos = (float) Math.cos(ang), sin = (float) Math.sin(ang);
            float mx = s.cx + s.dirX * off + (ft > 0 ? s.driftX * (ft / 1000f) : 0f);
            float my = s.cy + s.dirY * off + fall;

            float[] vx = new float[4], vy = new float[4];
            for (int v = 0; v < 4; v++) {
                float lx = s.x[v] - s.cx, ly = s.y[v] - s.cy;
                vx[v] = mx + lx * cos - ly * sin;
                vy[v] = my + lx * sin + ly * cos;
            }
            int rgb = tint & 0xFFFFFF;
            int fill = (Math.round(255 * s.alpha * fade) << 24) | rgb;
            quad(vc, m, vx, vy, fill);

            int edge = (Math.round(150 * fade) << 24) | 0xFFFFFF;
            for (int v = 0; v < 4; v++) {
                int n = (v + 1) % 4;
                if (vx[v] == vx[n] && vy[v] == vy[n]) continue;
                line(vc, m, vx[v], vy[v], vx[n], vy[n], 0.9f, edge);
            }
        }
        if (!any) SHARDS.clear();
    }

    /** Wypelniony czworokat; kolejnosc wierzcholkow dopasowana do tej uzywanej przez DrawContext.fill. */
    private static void quad(VertexConsumer vc, Matrix4f m, float[] x, float[] y, int argb) {
        float area = 0;
        for (int i = 0; i < 4; i++) {
            int n = (i + 1) % 4;
            area += x[i] * y[n] - x[n] * y[i];
        }
        if (Math.abs(area) < 0.5f) return;
        if (area < 0) {
            for (int i = 0; i < 4; i++) vc.vertex(m, x[i], y[i], 0f).color(argb);
        } else {
            for (int i = 3; i >= 0; i--) vc.vertex(m, x[i], y[i], 0f).color(argb);
        }
    }

    private static void line(VertexConsumer vc, Matrix4f m, float x0, float y0, float x1, float y1, float width, int argb) {
        float dx = x1 - x0, dy = y1 - y0;
        float len = (float) Math.hypot(dx, dy);
        if (len < 0.5f) return;
        float nx = -dy / len * width / 2f, ny = dx / len * width / 2f;
        quad(vc, m,
                new float[]{x0 - nx, x1 - nx, x1 + nx, x0 + nx},
                new float[]{y0 - ny, y1 - ny, y1 + ny, y0 + ny}, argb);
    }
}
