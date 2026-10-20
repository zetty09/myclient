package com.myclient.visuals;

import com.myclient.modules.JumpCirclesModule;
import com.myclient.modules.ModuleManager;
import com.myclient.ui.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Wykrywa skok gracza i rysuje rozchodzace sie kola na ziemi (rzutowane na ekran jak Target ESP). */
public final class JumpCircles {
    private record Circle(double x, double y, double z, long startMs) {}

    private static final List<Circle> CIRCLES = new ArrayList<>();
    private static boolean wasOnGround = true;

    private JumpCircles() {}

    public static void tick(MinecraftClient c) {
        ClientPlayerEntity p = c.player;
        if (p == null) {
            CIRCLES.clear();
            wasOnGround = true;
            return;
        }
        boolean on = p.isOnGround();
        if (ModuleManager.JUMP_CIRCLES.enabled && wasOnGround && !on && p.getVelocity().y > 0.1 && !p.isTouchingWater()) {
            CIRCLES.add(new Circle(p.getX(), p.getY() + 0.02, p.getZ(), System.currentTimeMillis()));
            if (CIRCLES.size() > 8) CIRCLES.remove(0);
        }
        wasOnGround = on;
    }

    public static void render(DrawContext ctx, MinecraftClient c) {
        if (CIRCLES.isEmpty() || c.player == null) return;
        JumpCirclesModule m = ModuleManager.JUMP_CIRCLES;
        Camera cam = c.gameRenderer.getCamera();
        float fov = c.options.getFov().getValue();
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        long now = System.currentTimeMillis();
        long durMs = (long) (m.duration.value * 1000f);
        int col = Colors.pick(m.color.get());
        int th = Math.round(m.thickness.value);

        Iterator<Circle> it = CIRCLES.iterator();
        while (it.hasNext()) {
            Circle ci = it.next();
            float t = (now - ci.startMs()) / (float) durMs;
            if (t >= 1f) {
                it.remove();
                continue;
            }
            float ease = 1f - (1f - t) * (1f - t) * (1f - t); // szybki start, wyhamowanie
            double r = 0.3 + (m.radius.value - 0.3) * ease;
            float fade = 1f - t;
            try {
                Gfx.alpha = fade;
                TargetEsp.ringAt(ctx, cam, fov, sw, sh, ci.x(), ci.y(), ci.z(), r, col, th);
                Gfx.alpha = fade * 0.5f;
                TargetEsp.ringAt(ctx, cam, fov, sw, sh, ci.x(), ci.y(), ci.z(), r * 0.72, col, Math.max(1, th - 1));
            } finally {
                Gfx.alpha = 1f;
            }
        }
    }
}
