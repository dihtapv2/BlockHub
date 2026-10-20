package de.blockhub.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Locale;
import java.util.function.Function;

public class NumberSetting extends Setting {
    public final double min, max, step, def;
    /** Optionale Anzeige-Formatierung (z. B. Uhrzeit). */
    public Function<Double, String> fmt;
    private double value;

    public NumberSetting(String id, String name, double def, double min, double max, double step) {
        super(id, name);
        this.def = def;
        this.min = min;
        this.max = max;
        this.step = step;
        set(def);
    }

    public double get() { return value; }
    public int getInt() { return (int) Math.round(value); }
    public float getFloat() { return (float) value; }

    public void set(double v) {
        v = Math.round((v - min) / step) * step + min;
        value = Math.max(min, Math.min(max, v));
    }

    public String display() {
        if (fmt != null) return fmt.apply(value);
        if (step >= 1) return String.valueOf((int) Math.round(value));
        return String.format(Locale.ROOT, step >= 0.1 ? "%.1f" : "%.2f", value);
    }

    @Override public JsonElement save() { return new JsonPrimitive(value); }
    @Override public void load(JsonElement e) { set(e.getAsDouble()); }
    @Override public void reset() { set(def); }
}
