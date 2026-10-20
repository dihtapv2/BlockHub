package de.blockhub.client.util;

import java.util.ArrayDeque;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class CpsTracker {
    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>(), RIGHT = new ArrayDeque<>();
    private static boolean lastL, lastR;

    private CpsTracker() {}

    /** Pro Frame aufrufen. */
    public static void poll(MinecraftClient mc) {
        long h = mc.getWindow().getHandle();
        boolean inGame = mc.currentScreen == null;
        boolean l = inGame && GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = inGame && GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        long now = System.currentTimeMillis();
        if (l && !lastL) LEFT.addLast(now);
        if (r && !lastR) RIGHT.addLast(now);
        lastL = l;
        lastR = r;
        while (!LEFT.isEmpty() && now - LEFT.peekFirst() > 1000) LEFT.removeFirst();
        while (!RIGHT.isEmpty() && now - RIGHT.peekFirst() > 1000) RIGHT.removeFirst();
    }

    public static int left() { return LEFT.size(); }
    public static int right() { return RIGHT.size(); }
}
