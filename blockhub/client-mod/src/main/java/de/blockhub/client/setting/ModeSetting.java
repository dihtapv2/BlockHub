package de.blockhub.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ModeSetting extends Setting {
    public final String[] options;
    private final int def;
    private int index;

    public ModeSetting(String id, String name, String[] options, int def) {
        super(id, name);
        this.options = options;
        this.def = def;
        this.index = def;
    }

    public int getIndex() { return index; }
    public String get() { return options[index]; }
    public void next() { index = (index + 1) % options.length; }
    public void prev() { index = (index + options.length - 1) % options.length; }

    @Override public JsonElement save() { return new JsonPrimitive(options[index]); }
    @Override public void load(JsonElement e) {
        String s = e.getAsString();
        for (int i = 0; i < options.length; i++) if (options[i].equals(s)) { index = i; return; }
    }
    @Override public void reset() { index = def; }
}
