package de.blockhub.client;

import de.blockhub.client.feature.Feature;
import de.blockhub.client.feature.Features;
import de.blockhub.client.feature.HudFeature;
import de.blockhub.client.gui.ModMenuScreen;
import de.blockhub.client.util.CpsTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockHubClient implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("BlockHub");
    public static KeyBinding MENU_KEY, ZOOM_KEY, FREELOOK_KEY;

    @Override
    public void onInitializeClient() {
        MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.blockhub.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.blockhub"));
        ZOOM_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.blockhub.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.categories.blockhub"));
        FREELOOK_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.blockhub.freelook", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.categories.blockhub"));

        ConfigManager.load();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (MENU_KEY.wasPressed()) {
                if (mc.currentScreen == null && mc.player != null) mc.setScreen(new ModMenuScreen());
            }
            for (Feature f : Features.ALL) {
                try { f.onTick(mc); } catch (Exception e) { LOG.warn("Fehler in Feature " + f.id, e); }
            }
        });

        HudRenderCallback.EVENT.register((ctx, tickDelta) -> renderHud(ctx));

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
            for (Feature f : Features.ALL) f.onShutdown(mc);
            ConfigManager.save();
        });

        LOG.info("BlockHub Client geladen – Menü mit Rechts-Shift.");
    }

    private static void renderHud(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        CpsTracker.poll(mc);
        if (mc.options.hudHidden) return;
        Features.CROSSHAIR.render(ctx, mc);
        if (mc.options.debugEnabled) return;
        for (HudFeature h : Features.HUDS) {
            if (!h.isEnabled()) continue;
            try { h.render(ctx, mc); } catch (Exception e) { LOG.warn("HUD-Fehler in " + h.id, e); }
        }
    }
}
