package de.blockhub.client.gui;

import de.blockhub.client.BlockHubClient;
import de.blockhub.client.ConfigManager;
import de.blockhub.client.feature.Category;
import de.blockhub.client.feature.Feature;
import de.blockhub.client.feature.Features;
import de.blockhub.client.setting.BoolSetting;
import de.blockhub.client.setting.ColorSetting;
import de.blockhub.client.setting.ModeSetting;
import de.blockhub.client.setting.NumberSetting;
import de.blockhub.client.setting.Setting;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

/**
 * Mod-Menü im Spiel (Rechts-Shift). Komplett selbst gezeichnet:
 * links Kategorien, Mitte Feature-Liste mit Suche, rechts die Einstellungen des gewählten Features.
 */
public class ModMenuScreen extends Screen {
    private interface Click { void on(double mx, double my, int button); }
    private interface Drag { void on(double mx); }
    private record Hit(int x1, int y1, int x2, int y2, Click click, Drag drag) {}

    private static final int ACCENT = 0xFF3B82F6, ACCENT_LIGHT = 0xFF8DB8FF;
    private static final int BG = 0xF0121216, SIDE = 0xFF0D0D11, ROW = 0xFF1B1B21, ROW_HOVER = 0xFF25252D;
    private static final int TXT = 0xFFE6E6EA, DIM = 0xFF8A8A96, OFF = 0xFF3A3A44, BORDER = 0xFF2A2A33, WARN = 0xFFE0B341;
    private static final int HEAD = 28, ROW_H = 24;
    private static final int[] PRESETS = {
        0xFFFFFF, 0xAAAAAA, 0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x3B82F6, 0xAA55FF, 0xFF55FF, 0x000000
    };

    // Auswahl bleibt beim erneuten Öffnen erhalten
    private static Category lastCat;
    private static Feature lastSelected;

    private Category cat = lastCat;
    private Feature selected = lastSelected != null ? lastSelected : Features.ALL.get(0);
    private String search = "";
    private boolean searchFocus;
    private double scrollList, scrollSet;
    private int contentList, contentSet;
    private final List<Hit> hits = new ArrayList<>();
    private Drag dragging;
    private int px, py, pw, ph, sideW, listX, listW, setX, setW;

    public ModMenuScreen() { super(Text.literal("BlockHub")); }

    @Override public boolean shouldPause() { return false; }

    @Override public void removed() {
        lastCat = cat;
        lastSelected = selected;
        ConfigManager.save();
    }

    // ------------------------------------------------------------------ Helfer
    private void layout() {
        pw = Math.min(width - 16, 660);
        ph = Math.min(height - 16, 360);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        sideW = pw < 480 ? 72 : 96;
        listX = px + sideW;
        listW = (int) ((pw - sideW) * 0.46);
        setX = listX + listW;
        setW = pw - sideW - listW;
    }

    private void hit(int x1, int y1, int x2, int y2, int clipT, int clipB, Click c, Drag d) {
        y1 = Math.max(y1, clipT);
        y2 = Math.min(y2, clipB);
        if (y2 <= y1 || x2 <= x1) return;
        hits.add(new Hit(x1, y1, x2, y2, c, d));
    }

    private static boolean in(int mx, int my, int x1, int y1, int x2, int y2, int clipT, int clipB) {
        return mx >= x1 && mx < x2 && my >= Math.max(y1, clipT) && my < Math.min(y2, clipB);
    }

    private void txt(DrawContext ctx, String s, int x, int y, int c) { ctx.drawText(textRenderer, s, x, y, c, false); }
    private void txtR(DrawContext ctx, String s, int xr, int y, int c) { txt(ctx, s, xr - textRenderer.getWidth(s), y, c); }

    private void sw(DrawContext ctx, int x, int y, boolean on) {
        ctx.fill(x, y, x + 24, y + 12, on ? ACCENT : OFF);
        int kx = on ? x + 14 : x + 2;
        ctx.fill(kx, y + 2, kx + 8, y + 10, 0xFFFFFFFF);
    }

    private List<String> wrap(String s, int maxW) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String word : s.split(" ")) {
            String t = cur.length() == 0 ? word : cur + " " + word;
            if (textRenderer.getWidth(t) > maxW && cur.length() > 0) {
                out.add(cur.toString());
                cur = new StringBuilder(word);
            } else cur = new StringBuilder(t);
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }

    private List<Feature> filtered() {
        String q = search.trim().toLowerCase(Locale.ROOT);
        List<Feature> out = new ArrayList<>();
        for (Feature f : Features.ALL) {
            if (cat != null && f.category != cat) continue;
            if (!q.isEmpty() && !f.name.toLowerCase(Locale.ROOT).contains(q) && !f.description.toLowerCase(Locale.ROOT).contains(q)) continue;
            out.add(f);
        }
        return out;
    }

    static int hue(double h) {
        h = Math.max(0, Math.min(0.9999, h)) * 6;
        int i = (int) h;
        double f = h - i, q = 1 - f, r, g, b;
        switch (i) {
            case 0 -> { r = 1; g = f; b = 0; }
            case 1 -> { r = q; g = 1; b = 0; }
            case 2 -> { r = 0; g = 1; b = f; }
            case 3 -> { r = 0; g = q; b = 1; }
            case 4 -> { r = f; g = 0; b = 1; }
            default -> { r = 1; g = 0; b = q; }
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    // ------------------------------------------------------------------ Rendering
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        hits.clear();
        layout();
        ctx.fill(0, 0, width, height, 0x99000000);
        ctx.fill(px - 1, py - 1, px + pw + 1, py + ph + 1, BORDER);
        ctx.fill(px, py, px + pw, py + ph, BG);

        // Kopfzeile
        ctx.fill(px, py, px + pw, py + HEAD, SIDE);
        ctx.fill(px, py + HEAD, px + pw, py + HEAD + 1, BORDER);
        ctx.drawText(textRenderer, Text.literal("BlockHub").formatted(Formatting.BOLD), px + 10, py + 10, ACCENT_LIGHT, false);
        txt(ctx, "Client", px + 10 + textRenderer.getWidth("BlockHub ") + 2, py + 10, DIM);
        String key = BlockHubClient.MENU_KEY == null ? "Rechts-Shift" : BlockHubClient.MENU_KEY.getBoundKeyLocalizedText().getString();
        txtR(ctx, key + " = schließen", px + pw - 10, py + 10, DIM);

        drawSidebar(ctx, mx, my);
        drawList(ctx, mx, my);
        drawSettings(ctx, mx, my);
    }

    private void drawSidebar(DrawContext ctx, int mx, int my) {
        ctx.fill(px, py + HEAD + 1, px + sideW, py + ph, SIDE);
        int y = py + HEAD + 8;
        List<Category> cats = new ArrayList<>();
        cats.add(null);
        for (Category c : Category.values()) cats.add(c);
        for (Category c : cats) {
            boolean sel = c == cat;
            int x1 = px + 6, x2 = px + sideW - 6;
            boolean hov = in(mx, my, x1, y, x2, y + 20, 0, height);
            if (sel) ctx.fill(x1, y, x2, y + 20, 0xFF22304A);
            else if (hov) ctx.fill(x1, y, x2, y + 20, ROW_HOVER);
            if (sel) ctx.fill(x1, y, x1 + 2, y + 20, ACCENT);
            txt(ctx, c == null ? "Alle" : c.label, x1 + 8, y + 6, sel ? TXT : DIM);
            final Category cc = c;
            hit(x1, y, x2, y + 20, 0, height, (a, b, btn) -> { cat = cc; scrollList = 0; }, null);
            y += 22;
        }
        int on = 0;
        for (Feature f : Features.ALL) if (f.isEnabled()) on++;
        txt(ctx, on + "/" + Features.ALL.size() + " aktiv", px + 10, py + ph - 14, DIM);
    }

    private void drawList(DrawContext ctx, int mx, int my) {
        int lx = listX + 6, lw = listW - 12;
        int sy = py + HEAD + 6;

        // Suchfeld
        ctx.fill(lx - 1, sy - 1, lx + lw + 1, sy + 17, searchFocus ? ACCENT : BORDER);
        ctx.fill(lx, sy, lx + lw, sy + 16, searchFocus ? 0xFF2B2B36 : ROW);
        boolean caret = searchFocus && (System.currentTimeMillis() / 500) % 2 == 0;
        if (search.isEmpty() && !searchFocus) txt(ctx, "Suchen ...", lx + 5, sy + 4, DIM);
        else txt(ctx, search + (caret ? "_" : ""), lx + 5, sy + 4, TXT);
        hit(lx, sy, lx + lw, sy + 16, 0, height, (a, b, btn) -> searchFocus = true, null);

        int ay1 = sy + 22, ay2 = py + ph - 6;
        List<Feature> list = filtered();
        contentList = list.size() * ROW_H;
        scrollList = Math.max(0, Math.min(scrollList, Math.max(0, contentList - (ay2 - ay1))));

        ctx.enableScissor(lx, ay1, lx + lw, ay2);
        if (list.isEmpty()) txt(ctx, "Nichts gefunden.", lx + 4, ay1 + 6, DIM);
        for (int i = 0; i < list.size(); i++) {
            Feature f = list.get(i);
            int ry = ay1 + i * ROW_H - (int) scrollList;
            if (ry + ROW_H < ay1 || ry > ay2) continue;
            boolean sel = f == selected;
            boolean hov = in(mx, my, lx, ry, lx + lw, ry + ROW_H - 2, ay1, ay2);
            ctx.fill(lx, ry, lx + lw - 6, ry + ROW_H - 2, sel ? 0xFF22304A : hov ? ROW_HOVER : ROW);
            if (sel) ctx.fill(lx, ry, lx + 2, ry + ROW_H - 2, ACCENT);
            txt(ctx, f.name, lx + 8, ry + 7, f.isEnabled() ? TXT : DIM);
            hit(lx, ry, lx + lw - 6, ry + ROW_H - 2, ay1, ay2, (a, b, btn) -> selected = f, null);
            int sx = lx + lw - 6 - 30, sy2 = ry + 5;
            sw(ctx, sx, sy2, f.isEnabled());
            hit(sx - 2, ry, lx + lw - 6, ry + ROW_H - 2, ay1, ay2, (a, b, btn) -> { f.toggle(); selected = f; }, null);
        }
        ctx.disableScissor();

        int view = ay2 - ay1;
        if (contentList > view) {
            int bh = Math.max(12, view * view / contentList);
            int by = ay1 + (int) ((view - bh) * (scrollList / (contentList - view)));
            ctx.fill(lx + lw - 3, by, lx + lw - 1, by + bh, 0xFF555560);
        }
    }

    private void drawSettings(DrawContext ctx, int mx, int my) {
        ctx.fill(setX, py + HEAD + 1, px + pw, py + ph, SIDE);
        int ay1 = py + HEAD + 8, ay2 = py + ph - 6;
        int x = setX + 8, w = setW - 16;
        Feature f = selected;
        if (f == null) return;

        scrollSet = Math.max(0, Math.min(scrollSet, Math.max(0, contentSet - (ay2 - ay1))));
        ctx.enableScissor(setX, ay1, px + pw, ay2);
        int y = ay1 - (int) scrollSet;

        ctx.drawText(textRenderer, Text.literal(f.name).formatted(Formatting.BOLD), x, y + 2, TXT, false);
        sw(ctx, x + w - 24, y, f.isEnabled());
        hit(x + w - 28, y - 2, x + w, y + 14, ay1, ay2, (a, b, btn) -> f.toggle(), null);
        y += 18;
        for (String l : wrap(f.description, w)) { txt(ctx, l, x, y, DIM); y += 10; }
        if (f.note != null) for (String l : wrap(f.note.get(), w)) { txt(ctx, l, x, y, WARN); y += 10; }
        y += 6;

        if (f.settings.isEmpty()) {
            txt(ctx, "Keine weiteren Einstellungen.", x, y, DIM);
            y += 14;
        } else {
            boolean hov = in(mx, my, x, y, x + 84, y + 14, ay1, ay2);
            ctx.fill(x, y, x + 84, y + 14, hov ? ROW_HOVER : ROW);
            txt(ctx, "Zurücksetzen", x + 8, y + 3, DIM);
            hit(x, y, x + 84, y + 14, ay1, ay2, (a, b, btn) -> f.resetSettings(), null);
            y += 20;
            ctx.fill(x, y, x + w, y + 1, BORDER);
            y += 8;
            for (Setting s : f.settings) {
                if (!s.visible.getAsBoolean()) continue;
                if (s instanceof BoolSetting b) y = drawBool(ctx, b, x, y, w, mx, my, ay1, ay2);
                else if (s instanceof NumberSetting n) y = drawNumber(ctx, n, x, y, w, ay1, ay2);
                else if (s instanceof ModeSetting m) y = drawMode(ctx, m, x, y, w, mx, my, ay1, ay2);
                else if (s instanceof ColorSetting c) y = drawColor(ctx, c, x, y, w, ay1, ay2);
            }
        }
        ctx.disableScissor();
        contentSet = (y + (int) scrollSet) - ay1 + 8;
    }

    private int drawBool(DrawContext ctx, BoolSetting b, int x, int y, int w, int mx, int my, int cT, int cB) {
        txt(ctx, b.name, x, y + 2, TXT);
        sw(ctx, x + w - 24, y, b.get());
        hit(x, y - 2, x + w, y + 14, cT, cB, (a, c, btn) -> b.toggle(), null);
        return y + 20;
    }

    private int drawNumber(DrawContext ctx, NumberSetting n, int x, int y, int w, int cT, int cB) {
        txt(ctx, n.name, x, y, TXT);
        txtR(ctx, n.display(), x + w, y, ACCENT_LIGHT);
        int ty = y + 15;
        double frac = (n.get() - n.min) / (n.max - n.min);
        int fw = (int) (frac * w);
        ctx.fill(x, ty, x + w, ty + 4, OFF);
        ctx.fill(x, ty, x + fw, ty + 4, ACCENT);
        int kx = Math.max(x, Math.min(x + w - 4, x + fw - 2));
        ctx.fill(kx, ty - 3, kx + 4, ty + 7, 0xFFFFFFFF);
        final int fx = x, fwid = w;
        hit(x, y + 10, x + w, y + 26, cT, cB, null,
            m -> n.set(n.min + Math.max(0, Math.min(1, (m - fx) / (double) fwid)) * (n.max - n.min)));
        return y + 30;
    }

    private int drawMode(DrawContext ctx, ModeSetting m, int x, int y, int w, int mx, int my, int cT, int cB) {
        txt(ctx, m.name, x, y + 3, TXT);
        int bw = Math.max(60, w / 2), bx = x + w - bw;
        boolean hov = in(mx, my, bx, y, bx + bw, y + 14, cT, cB);
        ctx.fill(bx, y, bx + bw, y + 14, hov ? ROW_HOVER : ROW);
        String v = m.get();
        txt(ctx, v, bx + (bw - textRenderer.getWidth(v)) / 2, y + 3, ACCENT_LIGHT);
        hit(bx, y, bx + bw, y + 14, cT, cB, (a, b, btn) -> { if (btn == 1) m.prev(); else m.next(); }, null);
        return y + 22;
    }

    private int drawColor(DrawContext ctx, ColorSetting c, int x, int y, int w, int cT, int cB) {
        txt(ctx, c.name, x, y, TXT);
        ctx.fill(x + w - 14, y - 2, x + w, y + 10, 0xFF555560);
        ctx.fill(x + w - 13, y - 1, x + w - 1, y + 9, 0xFF000000 | c.get());

        int n = PRESETS.length, gap = 2;
        int sz = Math.max(6, Math.min(14, (w - (n - 1) * gap) / n));
        int sy = y + 14;
        for (int i = 0; i < n; i++) {
            int sx = x + i * (sz + gap), col = PRESETS[i];
            if (c.get() == col) ctx.fill(sx - 1, sy - 1, sx + sz + 1, sy + sz + 1, 0xFFFFFFFF);
            else ctx.fill(sx - 1, sy - 1, sx + sz + 1, sy + sz + 1, 0xFF555560);
            ctx.fill(sx, sy, sx + sz, sy + sz, 0xFF000000 | col);
            hit(sx, sy, sx + sz, sy + sz, cT, cB, (a, b, btn) -> c.set(col), null);
        }

        int hy = sy + sz + 6;
        for (int i = 0; i < w; i++) ctx.fill(x + i, hy, x + i + 1, hy + 8, 0xFF000000 | hue(i / (double) w));
        final int fx = x, fw = w;
        hit(x, hy - 2, x + w, hy + 10, cT, cB, null, m -> c.set(hue((m - fx) / (double) fw)));
        return hy + 8 + 12;
    }

    // ------------------------------------------------------------------ Eingaben
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        searchFocus = false;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (mx >= h.x1() && mx < h.x2() && my >= h.y1() && my < h.y2()) {
                if (h.click() != null) h.click().on(mx, my, button);
                if (h.drag() != null && button == 0) {
                    dragging = h.drag();
                    dragging.on(mx);
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) { dragging.on(mx); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging != null) { dragging = null; ConfigManager.save(); }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (mx >= setX) scrollSet -= v * 14;
        else if (mx >= listX) scrollList -= v * 14;
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (BlockHubClient.MENU_KEY != null && BlockHubClient.MENU_KEY.matchesKey(key, scan)) {
            close();
            return true;
        }
        if (searchFocus) {
            if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) search = search.substring(0, search.length() - 1);
            else if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) searchFocus = false;
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean charTyped(char chr, int mods) {
        if (searchFocus && chr >= 32 && chr != 127 && search.length() < 24) {
            search += chr;
            scrollList = 0;
            return true;
        }
        return super.charTyped(chr, mods);
    }
}
