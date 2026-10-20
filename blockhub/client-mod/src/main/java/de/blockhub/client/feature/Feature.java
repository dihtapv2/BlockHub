package de.blockhub.client.feature;

import de.blockhub.client.setting.Setting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;

public class Feature {
    public final String id, name, description;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();
    /** Optionaler Hinweistext im Menü (z. B. aktuelle Taste). */
    public Supplier<String> note;
    private boolean enabled;

    public Feature(String id, String name, String description, Category category, boolean enabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = enabled;
    }

    protected <S extends Setting> S add(S s) {
        settings.add(s);
        return s;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { enabled = v; }
    public void toggle() { enabled = !enabled; }
    public void resetSettings() { for (Setting s : settings) s.reset(); }

    /** Wird jeden Client-Tick aufgerufen (auch wenn deaktiviert, damit aufgeräumt werden kann). */
    public void onTick(MinecraftClient mc) {}
    /** Beim Beenden des Spiels: Originalwerte wiederherstellen. */
    public void onShutdown(MinecraftClient mc) {}
}
