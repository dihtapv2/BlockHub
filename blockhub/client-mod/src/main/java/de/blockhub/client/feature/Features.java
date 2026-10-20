package de.blockhub.client.feature;

import java.util.ArrayList;
import java.util.List;

/** Zentrale Liste aller Features. Reihenfolge = Reihenfolge im Menü. */
public final class Features {
    public static final List<Feature> ALL = new ArrayList<>();
    public static final List<HudFeature> HUDS = new ArrayList<>();

    private Features() {}

    private static <T extends Feature> T reg(T f) {
        ALL.add(f);
        if (f instanceof HudFeature h) HUDS.add(h);
        return f;
    }

    // --- HUD
    public static final HudFeatures.Fps FPS = reg(new HudFeatures.Fps());
    public static final HudFeatures.Cps CPS = reg(new HudFeatures.Cps());
    public static final HudFeatures.Coords COORDS = reg(new HudFeatures.Coords());
    public static final HudFeatures.Ping PING = reg(new HudFeatures.Ping());
    public static final HudFeatures.Keystrokes KEYSTROKES = reg(new HudFeatures.Keystrokes());
    public static final HudFeatures.Armor ARMOR = reg(new HudFeatures.Armor());
    public static final HudFeatures.Potions POTIONS = reg(new HudFeatures.Potions());
    public static final HudFeatures.Clock CLOCK = reg(new HudFeatures.Clock());
    public static final HudFeatures.Speed SPEED = reg(new HudFeatures.Speed());
    public static final HudFeatures.Memory MEMORY = reg(new HudFeatures.Memory());

    // --- Kamera
    public static final CoreFeatures.Zoom ZOOM = reg(new CoreFeatures.Zoom());
    public static final CoreFeatures.Freelook FREELOOK = reg(new CoreFeatures.Freelook());
    public static final Feature NO_HURT_CAM = reg(new Feature("no_hurt_cam", "Kein Hurtcam",
            "Entfernt das Kamerawackeln, wenn du Schaden nimmst.", Category.CAMERA, false));
    public static final Feature NO_BOB = reg(new Feature("no_bob", "Kein View-Bobbing",
            "Die Kamera wackelt beim Laufen nicht mehr.", Category.CAMERA, false));

    // --- Grafik
    public static final CoreFeatures.Fullbright FULLBRIGHT = reg(new CoreFeatures.Fullbright());
    public static final CoreFeatures.Crosshair CROSSHAIR = reg(new CoreFeatures.Crosshair());
    public static final Feature NO_FIRE = reg(new Feature("no_fire", "Kein Feuer-Overlay",
            "Blendet das Feuer vor deinem Bildschirm aus, wenn du brennst.", Category.VISUAL, false));

    // --- Welt
    public static final CoreFeatures.TimeChanger TIME = reg(new CoreFeatures.TimeChanger());
    public static final CoreFeatures.WeatherChanger WEATHER = reg(new CoreFeatures.WeatherChanger());

    // --- Gameplay
    public static final CoreFeatures.AutoSprint AUTO_SPRINT = reg(new CoreFeatures.AutoSprint());
    public static final CoreFeatures.Hitboxes HITBOXES = reg(new CoreFeatures.Hitboxes());
}
