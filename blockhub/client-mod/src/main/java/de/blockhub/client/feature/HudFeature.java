package de.blockhub.client.feature;

import de.blockhub.client.gui.ModMenuScreen;
import de.blockhub.client.setting.BoolSetting;
import de.blockhub.client.setting.ColorSetting;
import de.blockhub.client.setting.NumberSetting;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

/** Basis für alle HUD-Elemente: Position, Größe, Farbe, Hintergrund und Schatten sind einstellbar. */
public class HudFeature extends Feature {
    public final NumberSetting posX, posY, scale, bgAlpha;
    public final ColorSetting color;
    public final BoolSetting shadow;

    public HudFeature(String id, String name, String desc, double x, double y, boolean on) {
        super(id, name, desc, Category.HUD, on);
        posX = add(new NumberSetting("x", "Position X (%)", x, 0, 100, 1));
        posY = add(new NumberSetting("y", "Position Y (%)", y, 0, 100, 1));
        scale = add(new NumberSetting("scale", "Größe", 1.0, 0.5, 2.5, 0.1));
        color = add(new ColorSetting("color", "Textfarbe", 0xFFFFFF));
        bgAlpha = add(new NumberSetting("bg", "Hintergrund (%)", 35, 0, 100, 5));
        shadow = add(new BoolSetting("shadow", "Textschatten", true));
    }

    /** Zeilen für das Standard-Textlayout. */
    protected List<String> lines(MinecraftClient mc) { return List.of(); }

    /** True, solange das Mod-Menü offen ist (damit leere Elemente als Vorschau sichtbar sind). */
    protected static boolean preview(MinecraftClient mc) { return mc.currentScreen instanceof ModMenuScreen; }

    public void render(DrawContext ctx, MinecraftClient mc) {
        List<String> ls = lines(mc);
        if (ls.isEmpty()) return;
        int w = 0;
        for (String l : ls) w = Math.max(w, mc.textRenderer.getWidth(l));
        int bw = w + 6, bh = ls.size() * 10 + 3;
        begin(ctx, mc, bw, bh, true);
        for (int i = 0; i < ls.size(); i++) text(ctx, mc, ls.get(i), 3, 3 + i * 10, col());
        end(ctx);
    }

    protected void begin(DrawContext ctx, MinecraftClient mc, int bw, int bh, boolean background) {
        float s = scale.getFloat();
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        float x = (float) (posX.get() / 100.0 * Math.max(0, sw - bw * s));
        float y = (float) (posY.get() / 100.0 * Math.max(0, sh - bh * s));
        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(x, y, 0);
        m.scale(s, s, 1f);
        int a = (int) (bgAlpha.get() * 2.55);
        if (background && a > 0) ctx.fill(0, 0, bw, bh, a << 24);
    }

    protected void end(DrawContext ctx) { ctx.getMatrices().pop(); }

    protected int col() { return 0xFF000000 | color.get(); }

    protected void text(DrawContext ctx, MinecraftClient mc, String s, int x, int y, int c) {
        ctx.drawText(mc.textRenderer, s, x, y, c, shadow.get());
    }
}
