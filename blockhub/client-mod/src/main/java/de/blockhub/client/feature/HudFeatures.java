package de.blockhub.client.feature;

import de.blockhub.client.setting.BoolSetting;
import de.blockhub.client.setting.ModeSetting;
import de.blockhub.client.setting.NumberSetting;
import de.blockhub.client.util.CpsTracker;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;

public final class HudFeatures {
    private HudFeatures() {}

    // ------------------------------------------------------------------ FPS
    public static final class Fps extends HudFeature {
        public Fps() { super("fps", "FPS", "Zeigt die aktuellen Bilder pro Sekunde.", 1, 1, true); }
        @Override protected List<String> lines(MinecraftClient mc) { return List.of("FPS: " + mc.getCurrentFps()); }
    }

    // ------------------------------------------------------------------ CPS
    public static final class Cps extends HudFeature {
        public final BoolSetting right = add(new BoolSetting("right", "Rechtsklick anzeigen", true));
        public Cps() { super("cps", "CPS", "Klicks pro Sekunde (links und rechts).", 1, 7, true); }
        @Override protected List<String> lines(MinecraftClient mc) {
            return List.of("CPS: " + CpsTracker.left() + (right.get() ? " | " + CpsTracker.right() : ""));
        }
    }

    // ------------------------------------------------------------------ Koordinaten
    public static final class Coords extends HudFeature {
        public final NumberSetting decimals = add(new NumberSetting("decimals", "Nachkommastellen", 1, 0, 2, 1));
        public final BoolSetting oneLine = add(new BoolSetting("oneline", "In einer Zeile", false));
        public final BoolSetting facing = add(new BoolSetting("facing", "Blickrichtung zeigen", true));
        public Coords() { super("coords", "Koordinaten", "Zeigt deine Position und Blickrichtung.", 1, 13, false); }
        @Override protected List<String> lines(MinecraftClient mc) {
            var p = mc.player;
            String f = "%." + decimals.getInt() + "f";
            String x = String.format(Locale.ROOT, f, p.getX()), y = String.format(Locale.ROOT, f, p.getY()), z = String.format(Locale.ROOT, f, p.getZ());
            List<String> out = new ArrayList<>();
            if (oneLine.get()) out.add("XYZ: " + x + " / " + y + " / " + z);
            else { out.add("X: " + x); out.add("Y: " + y); out.add("Z: " + z); }
            if (facing.get()) out.add("Blick: " + dir(p.getHorizontalFacing()));
            return out;
        }
        private static String dir(Direction d) {
            return switch (d) {
                case NORTH -> "Norden";
                case SOUTH -> "Süden";
                case EAST -> "Osten";
                case WEST -> "Westen";
                default -> d.asString();
            };
        }
    }

    // ------------------------------------------------------------------ Ping
    public static final class Ping extends HudFeature {
        public Ping() { super("ping", "Ping", "Zeigt deine Latenz zum Server.", 1, 24, false); }
        @Override protected List<String> lines(MinecraftClient mc) {
            ClientPlayNetworkHandler h = mc.getNetworkHandler();
            PlayerListEntry e = h == null ? null : h.getPlayerListEntry(mc.player.getUuid());
            return List.of("Ping: " + (e == null ? "-" : e.getLatency() + " ms"));
        }
    }

    // ------------------------------------------------------------------ Geschwindigkeit
    public static final class Speed extends HudFeature {
        public Speed() { super("speed", "Geschwindigkeit", "Horizontale Geschwindigkeit in Blöcken pro Sekunde.", 1, 30, false); }
        @Override protected List<String> lines(MinecraftClient mc) {
            var p = mc.player;
            double v = Math.hypot(p.getX() - p.prevX, p.getZ() - p.prevZ) * 20.0;
            return List.of(String.format(Locale.ROOT, "Speed: %.2f b/s", v));
        }
    }

    // ------------------------------------------------------------------ Speicher
    public static final class Memory extends HudFeature {
        public Memory() { super("memory", "RAM-Anzeige", "Arbeitsspeicher-Verbrauch von Minecraft.", 1, 36, false); }
        @Override protected List<String> lines(MinecraftClient mc) {
            Runtime rt = Runtime.getRuntime();
            long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L, max = rt.maxMemory() / 1048576L;
            return List.of("RAM: " + used + "/" + max + " MB");
        }
    }

    // ------------------------------------------------------------------ Uhr
    public static final class Clock extends HudFeature {
        public final BoolSetting h24 = add(new BoolSetting("h24", "24-Stunden-Format", true));
        public final BoolSetting seconds = add(new BoolSetting("seconds", "Sekunden anzeigen", false));
        public Clock() { super("clock", "Uhr", "Zeigt die echte Uhrzeit.", 100, 1, false); }
        @Override protected List<String> lines(MinecraftClient mc) {
            String pat = (h24.get() ? "HH:mm" : "hh:mm") + (seconds.get() ? ":ss" : "") + (h24.get() ? "" : " a");
            return List.of(LocalTime.now().format(DateTimeFormatter.ofPattern(pat, Locale.ENGLISH)));
        }
    }

    // ------------------------------------------------------------------ Effekte
    public static final class Potions extends HudFeature {
        public Potions() { super("potions", "Effektanzeige", "Aktive Tränke-Effekte mit Restzeit.", 100, 10, false); }
        @Override protected List<String> lines(MinecraftClient mc) {
            List<String> out = new ArrayList<>();
            for (StatusEffectInstance e : mc.player.getStatusEffects()) {
                String n = e.getEffectType().value().getName().getString();
                int amp = e.getAmplifier();
                String t = e.isInfinite() ? "--" : String.format(Locale.ROOT, "%d:%02d", e.getDuration() / 1200, (e.getDuration() / 20) % 60);
                out.add(n + (amp > 0 ? " " + (amp + 1) : "") + " " + t);
            }
            if (out.isEmpty() && preview(mc)) out.add("Geschwindigkeit II 0:30");
            return out;
        }
    }

    // ------------------------------------------------------------------ Rüstung
    public static final class Armor extends HudFeature {
        public final ModeSetting mode = add(new ModeSetting("mode", "Haltbarkeit", new String[] {"Zahl", "Prozent", "Aus"}, 0));
        public final BoolSetting byDurability = add(new BoolSetting("bydur", "Farbe nach Haltbarkeit", true));
        public final BoolSetting hand = add(new BoolSetting("hand", "Item in der Hand zeigen", false));
        public Armor() { super("armor", "Rüstungsanzeige", "Zeigt deine Rüstung samt Haltbarkeit.", 100, 60, false); }

        @Override public void render(DrawContext ctx, MinecraftClient mc) {
            List<ItemStack> items = new ArrayList<>();
            for (int i = 3; i >= 0; i--) {
                ItemStack s = mc.player.getInventory().getArmorStack(i);
                if (!s.isEmpty()) items.add(s);
            }
            if (hand.get() && !mc.player.getMainHandStack().isEmpty()) items.add(mc.player.getMainHandStack());
            if (items.isEmpty() && preview(mc)) {
                items.add(new ItemStack(Items.DIAMOND_HELMET));
                items.add(new ItemStack(Items.DIAMOND_CHESTPLATE));
                items.add(new ItemStack(Items.DIAMOND_LEGGINGS));
                items.add(new ItemStack(Items.DIAMOND_BOOTS));
            }
            if (items.isEmpty()) return;
            int tw = 0;
            List<String> txt = new ArrayList<>();
            for (ItemStack s : items) {
                String t = label(s);
                txt.add(t);
                tw = Math.max(tw, mc.textRenderer.getWidth(t));
            }
            int rowH = 18, bw = 20 + (tw > 0 ? tw + 4 : 0), bh = items.size() * rowH + 2;
            begin(ctx, mc, bw, bh, true);
            for (int i = 0; i < items.size(); i++) {
                ItemStack s = items.get(i);
                int y = 1 + i * rowH;
                ctx.drawItem(s, 2, y);
                if (!txt.get(i).isEmpty()) {
                    int c = byDurability.get() && s.isDamageable() ? 0xFF000000 | s.getItemBarColor() : col();
                    text(ctx, mc, txt.get(i), 22, y + 5, c);
                }
            }
            end(ctx);
        }

        private String label(ItemStack s) {
            if (mode.getIndex() == 2) return "";
            if (!s.isDamageable()) return s.getCount() > 1 ? "x" + s.getCount() : "";
            int left = s.getMaxDamage() - s.getDamage();
            return mode.getIndex() == 0 ? String.valueOf(left) : (left * 100 / s.getMaxDamage()) + "%";
        }
    }

    // ------------------------------------------------------------------ Tastenanzeige
    public static final class Keystrokes extends HudFeature {
        public Keystrokes() { super("keystrokes", "Tastenanzeige", "Zeigt WASD, Leertaste und Mausklicks.", 1, 62, false); }

        @Override public void render(DrawContext ctx, MinecraftClient mc) {
            final int k = 18, g = 2;
            int bw = k * 3 + g * 2, bh = k * 4 + g * 3;
            var o = mc.options;
            begin(ctx, mc, bw, bh, false);
            key(ctx, mc, "W", k + g, 0, k, k, o.forwardKey.isPressed());
            key(ctx, mc, "A", 0, k + g, k, k, o.leftKey.isPressed());
            key(ctx, mc, "S", k + g, k + g, k, k, o.backKey.isPressed());
            key(ctx, mc, "D", (k + g) * 2, k + g, k, k, o.rightKey.isPressed());
            int half = (bw - g) / 2;
            key(ctx, mc, "LMB", 0, (k + g) * 2, half, k, o.attackKey.isPressed());
            key(ctx, mc, "RMB", half + g, (k + g) * 2, bw - half - g, k, o.useKey.isPressed());
            key(ctx, mc, "SPACE", 0, (k + g) * 3, bw, k, o.jumpKey.isPressed());
            end(ctx);
        }

        private void key(DrawContext ctx, MinecraftClient mc, String s, int x, int y, int w, int h, boolean down) {
            int a = Math.max(0x30, (int) (bgAlpha.get() * 2.55));
            ctx.fill(x, y, x + w, y + h, down ? 0xB0FFFFFF : (a << 24));
            int tx = x + (w - mc.textRenderer.getWidth(s)) / 2;
            ctx.drawText(mc.textRenderer, s, tx, y + (h - 8) / 2, down ? 0xFF000000 : col(), shadow.get() && !down);
        }
    }
}
