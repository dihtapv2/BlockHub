package de.blockhub.client.setting;

import com.google.gson.JsonElement;
import java.util.function.BooleanSupplier;

public abstract class Setting {
    public final String id, name;
    /** Setting wird im Menü nur gezeigt, wenn dies true liefert. */
    public BooleanSupplier visible = () -> true;

    protected Setting(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public abstract JsonElement save();
    public abstract void load(JsonElement e);
    public abstract void reset();
}
