package de.blockhub.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ColorSetting extends Setting {
    private final int def;
    private int rgb;

    public ColorSetting(String id, String name, int def) {
        super(id, name);
        this.def = def & 0xFFFFFF;
        this.rgb = this.def;
    }

    /** RGB ohne Alpha. */
    public int get() { return rgb; }
    public void set(int v) { rgb = v & 0xFFFFFF; }

    @Override public JsonElement save() { return new JsonPrimitive(rgb); }
    @Override public void load(JsonElement e) { set(e.getAsInt()); }
    @Override public void reset() { rgb = def; }
}
