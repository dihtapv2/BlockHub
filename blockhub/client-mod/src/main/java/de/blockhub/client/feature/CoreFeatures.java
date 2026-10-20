package de.blockhub.client.feature;

import de.blockhub.client.BlockHubClient;
import de.blockhub.client.mixin.SimpleOptionAccessor;
import de.blockhub.client.setting.BoolSetting;
import de.blockhub.client.setting.ColorSetting;
import de.blockhub.client.setting.ModeSetting;
import de.blockhub.client.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.network.ClientPlayerEntity;

public final class CoreFeatures {
    private CoreFeatures() {}

    /** Setzt einen Optionswert ohne die Slider-Begrenzung (z. B. Gamma > 1). */
    @SuppressWarnings("unchecked")
    static void forceSet(SimpleOption<Double> o, double v) {
        ((SimpleOptionAccessor<Double>) (Object) o).bh$setValue(v);
    }

    private static String keyName(net.minecraft.client.option.KeyBinding k) {
        return k == null ? "?" : k.getBoundKeyLocalizedText().getString();
    }

    // ------------------------------------------------------------------ Fullbright
    public static final class Fullbright extends Feature {
        public final NumberSetting level = add(new NumberSetting("level", "Helligkeit", 12, 2, 16, 1));
        private Double original;

        public Fullbright() { super("fullbright", "Fullbright", "Alles ist hell – auch in Höhlen und nachts.", Category.VISUAL, false); }

        @Override public void onTick(MinecraftClient mc) {
            SimpleOption<Double> g = mc.options.getGamma();
            if (isEnabled()) {
                if (original == null) original = g.getValue();
                forceSet(g, level.get());
            } else if (original != null) {
                forceSet(g, original);
                original = null;
            }
        }

        @Override public void onShutdown(MinecraftClient mc) {
            if (original != null) { forceSet(mc.options.getGamma(), original); original = null; }
        }
    }

    // ------------------------------------------------------------------ Fadenkreuz
    public static final class Crosshair extends Feature {
        public final NumberSetting size = add(new NumberSetting("size", "Länge", 5, 1, 20, 1));
        public final NumberSetting gap = add(new NumberSetting("gap", "Abstand zur Mitte", 2, 0, 12, 1));
        public final NumberSetting thickness = add(new NumberSetting("thick", "Dicke", 1, 1, 6, 1));
        public final NumberSetting opacity = add(new NumberSetting("opacity", "Deckkraft (%)", 100, 10, 100, 5));
        public final BoolSetting dot = add(new BoolSetting("dot", "Punkt in der Mitte", false));
        public final BoolSetting outline = add(new BoolSetting("outline", "Schwarzer Rand", true));
        public final ColorSetting color = add(new ColorSetting("color", "Farbe", 0xFFFFFF));

        public Crosshair() { super("crosshair", "Eigenes Fadenkreuz", "Ersetzt das Fadenkreuz durch ein anpassbares.", Category.VISUAL, false); }

        public boolean active() {
            MinecraftClient mc = MinecraftClient.getInstance();
            return isEnabled() && !mc.options.debugEnabled && mc.options.getPerspective().isFirstPerson();
        }

        public void render(DrawContext ctx, MinecraftClient mc) {
            if (!active()) return;
            int cx = mc.getWindow().getScaledWidth() / 2, cy = mc.getWindow().getScaledHeight() / 2;
            int a = Math.min(255, (int) (opacity.get() * 2.55));
            int c = (a << 24) | color.get();
            int s = size.getInt(), g = gap.getInt(), t = thickness.getInt(), h = t / 2;
            int[][] bars = {
                {cx - h, cy - g - s, cx - h + t, cy - g},
                {cx - h, cy + g, cx - h + t, cy + g + s},
                {cx - g - s, cy - h, cx - g, cy - h + t},
                {cx + g, cy - h, cx + g + s, cy - h + t},
                {cx - h, cy - h, cx - h + t, cy - h + t},
            };
            int n = dot.get() ? 5 : 4;
            if (outline.get()) for (int i = 0; i < n; i++) ctx.fill(bars[i][0] - 1, bars[i][1] - 1, bars[i][2] + 1, bars[i][3] + 1, a << 24);
            for (int i = 0; i < n; i++) ctx.fill(bars[i][0], bars[i][1], bars[i][2], bars[i][3], c);
        }
    }

    // ------------------------------------------------------------------ Hitboxen
    public static final class Hitboxes extends Feature {
        private boolean applied;
        public Hitboxes() { super("hitboxes", "Hitboxen", "Zeigt die Hitboxen von Entities (wie F3+B).", Category.GAMEPLAY, false); }

        @Override public void onTick(MinecraftClient mc) {
            if (mc.world == null) return;
            if (isEnabled() != applied) {
                mc.getEntityRenderDispatcher().setRenderHitboxes(isEnabled());
                applied = isEnabled();
            }
        }
    }

    // ------------------------------------------------------------------ Auto-Sprint
    public static final class AutoSprint extends Feature {
        public AutoSprint() { super("autosprint", "Auto-Sprint", "Du sprintest automatisch, sobald du vorwärts läufst.", Category.GAMEPLAY, false); }

        @Override public void onTick(MinecraftClient mc) {
            ClientPlayerEntity p = mc.player;
            if (!isEnabled() || p == null || mc.currentScreen != null) return;
            if (mc.options.forwardKey.isPressed() && !p.isSprinting() && !p.isSneaking() && !p.isUsingItem()
                    && !p.horizontalCollision && (p.getHungerManager().getFoodLevel() > 6 || p.isCreative())) {
                p.setSprinting(true);
            }
        }
    }

    // ------------------------------------------------------------------ Zoom
    public static final class Zoom extends Feature {
        public final NumberSetting factor = add(new NumberSetting("factor", "Zoom-Stärke", 4, 1.5, 10, 0.5));
        public final BoolSetting smooth = add(new BoolSetting("smooth", "Sanfter Übergang", true));
        public final BoolSetting sens = add(new BoolSetting("sens", "Maus-Empfindlichkeit anpassen", true));
        private double cur = 1.0;
        private long last = System.nanoTime();
        private Double origSens;

        public Zoom() {
            super("zoom", "Zoom", "Taste halten, um heranzuzoomen.", Category.CAMERA, true);
            note = () -> "Taste: " + keyName(BlockHubClient.ZOOM_KEY) + " (halten). Änderbar unter Steuerung.";
        }

        public boolean zooming(MinecraftClient mc) {
            return isEnabled() && BlockHubClient.ZOOM_KEY != null && mc.currentScreen == null && BlockHubClient.ZOOM_KEY.isPressed();
        }

        /** Faktor, mit dem das Sichtfeld multipliziert wird (1.0 = kein Zoom). */
        public double multiplier() {
            MinecraftClient mc = MinecraftClient.getInstance();
            double target = zooming(mc) ? 1.0 / factor.get() : 1.0;
            long now = System.nanoTime();
            double dt = Math.min(0.1, (now - last) / 1e9);
            last = now;
            cur = smooth.get() ? cur + (target - cur) * (1 - Math.exp(-dt * 14)) : target;
            if (Math.abs(cur - target) < 0.001) cur = target;
            return cur;
        }

        @Override public void onTick(MinecraftClient mc) {
            SimpleOption<Double> o = mc.options.getMouseSensitivity();
            if (zooming(mc) && sens.get()) {
                if (origSens == null) origSens = o.getValue();
                forceSet(o, origSens * Math.max(0.15, 1.0 / factor.get()));
            } else if (origSens != null) {
                forceSet(o, origSens);
                origSens = null;
            }
        }

        @Override public void onShutdown(MinecraftClient mc) {
            if (origSens != null) { forceSet(mc.options.getMouseSensitivity(), origSens); origSens = null; }
        }
    }

    // ------------------------------------------------------------------ Freelook
    public static final class Freelook extends Feature {
        public final BoolSetting thirdPerson = add(new BoolSetting("third", "In 3. Person wechseln", true));
        public final BoolSetting invert = add(new BoolSetting("invert", "Y-Achse invertieren", false));
        public boolean active;
        public float yaw, pitch;
        private Perspective prev;

        public Freelook() {
            super("freelook", "Freelook", "Taste halten und dich umsehen, ohne die Laufrichtung zu ändern.", Category.CAMERA, true);
            note = () -> "Taste: " + keyName(BlockHubClient.FREELOOK_KEY) + " (halten). Änderbar unter Steuerung.";
        }

        public void rotate(double dx, double dy) {
            yaw += (float) dx * 0.15f;
            pitch += (float) dy * 0.15f * (invert.get() ? -1 : 1);
            pitch = Math.max(-90f, Math.min(90f, pitch));
        }

        @Override public void onTick(MinecraftClient mc) {
            boolean want = isEnabled() && mc.player != null && mc.currentScreen == null
                    && BlockHubClient.FREELOOK_KEY != null && BlockHubClient.FREELOOK_KEY.isPressed();
            if (want && !active) {
                active = true;
                yaw = mc.player.getYaw();
                pitch = mc.player.getPitch();
                if (thirdPerson.get()) {
                    prev = mc.options.getPerspective();
                    mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                }
            } else if (!want && active) {
                active = false;
                if (prev != null) { mc.options.setPerspective(prev); prev = null; }
            }
        }
    }

    // ------------------------------------------------------------------ Zeit
    public static final class TimeChanger extends Feature {
        public final NumberSetting time = add(new NumberSetting("time", "Tageszeit", 6000, 0, 24000, 250));

        public TimeChanger() {
            super("time", "Zeit ändern", "Stellt die Tageszeit nur bei dir ein (Server merkt nichts).", Category.WORLD, false);
            time.fmt = v -> String.format("%02d:%02d", (int) ((v / 1000.0 + 6) % 24), (int) ((v % 1000) / 1000.0 * 60));
        }

        public long ticks() { return (long) time.get(); }
    }

    // ------------------------------------------------------------------ Wetter
    public static final class WeatherChanger extends Feature {
        public final ModeSetting mode = add(new ModeSetting("mode", "Wetter", new String[] {"Klar", "Regen", "Gewitter"}, 0));

        public WeatherChanger() { super("weather", "Wetter ändern", "Stellt das Wetter nur bei dir ein.", Category.WORLD, false); }

        public float rain() { return mode.getIndex() == 0 ? 0f : 1f; }
        public float thunder() { return mode.getIndex() == 2 ? 1f : 0f; }
    }
}
