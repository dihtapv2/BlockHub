package de.blockhub.client.feature;

public enum Category {
    HUD("HUD"),
    CAMERA("Kamera"),
    VISUAL("Grafik"),
    WORLD("Welt"),
    GAMEPLAY("Gameplay");

    public final String label;
    Category(String label) { this.label = label; }
}
