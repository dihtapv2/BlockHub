package de.blockhub.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BoolSetting extends Setting {
    private final boolean def;
    private boolean value;

    public BoolSetting(String id, String name, boolean def) {
        super(id, name);
        this.def = def;
        this.value = def;
    }

    public boolean get() { return value; }
    public void set(boolean v) { value = v; }
    public void toggle() { value = !value; }

    @Override public JsonElement save() { return new JsonPrimitive(value); }
    @Override public void load(JsonElement e) { value = e.getAsBoolean(); }
    @Override public void reset() { value = def; }
}
