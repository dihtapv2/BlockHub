package de.blockhub.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.blockhub.client.feature.Feature;
import de.blockhub.client.feature.Features;
import de.blockhub.client.setting.Setting;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Speichert/lädt alle Einstellungen in config/blockhub.json. */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigManager() {}

    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("blockhub.json"); }

    public static void load() {
        try {
            if (!Files.exists(file())) return;
            JsonObject root = JsonParser.parseString(Files.readString(file())).getAsJsonObject();
            JsonObject mods = root.getAsJsonObject("features");
            if (mods == null) return;
            for (Feature f : Features.ALL) {
                JsonObject o = mods.getAsJsonObject(f.id);
                if (o == null) continue;
                if (o.has("enabled")) f.setEnabled(o.get("enabled").getAsBoolean());
                JsonObject ss = o.getAsJsonObject("settings");
                if (ss == null) continue;
                for (Setting s : f.settings) {
                    if (!ss.has(s.id)) continue;
                    try { s.load(ss.get(s.id)); } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            BlockHubClient.LOG.warn("Konfiguration konnte nicht geladen werden", e);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject(), mods = new JsonObject();
            for (Feature f : Features.ALL) {
                JsonObject o = new JsonObject(), ss = new JsonObject();
                o.addProperty("enabled", f.isEnabled());
                for (Setting s : f.settings) ss.add(s.id, s.save());
                o.add("settings", ss);
                mods.add(f.id, o);
            }
            root.add("features", mods);
            Files.createDirectories(file().getParent());
            Files.writeString(file(), GSON.toJson(root));
        } catch (Exception e) {
            BlockHubClient.LOG.warn("Konfiguration konnte nicht gespeichert werden", e);
        }
    }
}
