package com.myclient.visuals;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/** Liczy kliki myszy z ostatniej sekundy (CPS). Wolane co klatke z HUD. */
public final class ClickTracker {
    private static final Deque<Long> LEFT = new ArrayDeque<>();
    private static final Deque<Long> RIGHT = new ArrayDeque<>();
    private static boolean wasLeft, wasRight;

    private ClickTracker() {}

    public static void update(MinecraftClient c) {
        if (c.getWindow() == null) return;
        long h = c.getWindow().getHandle();
        boolean inGame = c.currentScreen == null;
        boolean l = inGame && GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = inGame && GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        long now = System.currentTimeMillis();
        if (l && !wasLeft) LEFT.addLast(now);
        if (r && !wasRight) RIGHT.addLast(now);
        wasLeft = l;
        wasRight = r;
        prune(LEFT, now);
        prune(RIGHT, now);
    }

    private static void prune(Deque<Long> q, long now) {
        while (!q.isEmpty() && now - q.peekFirst() > 1000) q.pollFirst();
    }

    public static int left() { prune(LEFT, System.currentTimeMillis()); return LEFT.size(); }

    public static int right() { prune(RIGHT, System.currentTimeMillis()); return RIGHT.size(); }
}
