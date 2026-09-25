package com.zenxy.hologram.model;

import org.bukkit.Color;
import org.bukkit.entity.TextDisplay;

import java.util.HashMap;
import java.util.Map;

public class HologramLineData {

    private String text;
    private float scaleX;
    private float scaleY;
    private float scaleZ;
    private Color backgroundColor;
    private boolean shadow;
    private boolean seeThrough;
    private TextDisplay.TextAlignment alignment;

    public HologramLineData(String text) {
        this.text = text;
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.scaleZ = 1.0f;
        this.backgroundColor = null; // null means default transparan/server default
        this.shadow = false;
        this.seeThrough = false;
        this.alignment = TextDisplay.TextAlignment.CENTER;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public float getScaleZ() {
        return scaleZ;
    }

    public void setScaleZ(float scaleZ) {
        this.scaleZ = scaleZ;
    }

    public void setUniformScale(float scale) {
        this.scaleX = scale;
        this.scaleY = scale;
        this.scaleZ = scale;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isShadow() {
        return shadow;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public boolean isSeeThrough() {
        return seeThrough;
    }

    public void setSeeThrough(boolean seeThrough) {
        this.seeThrough = seeThrough;
    }

    public TextDisplay.TextAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(TextDisplay.TextAlignment alignment) {
        this.alignment = alignment;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("text", text);
        map.put("scaleX", scaleX);
        map.put("scaleY", scaleY);
        map.put("scaleZ", scaleZ);
        if (backgroundColor != null) {
            map.put("backgroundColor", backgroundColor.asARGB());
        }
        map.put("shadow", shadow);
        map.put("seeThrough", seeThrough);
        map.put("alignment", alignment.name());
        return map;
    }

    public static HologramLineData deserialize(Map<String, Object> map) {
        String text = (String) map.getOrDefault("text", "");
        HologramLineData line = new HologramLineData(text);
        if (map.containsKey("scaleX")) line.setScaleX(((Number) map.get("scaleX")).floatValue());
        if (map.containsKey("scaleY")) line.setScaleY(((Number) map.get("scaleY")).floatValue());
        if (map.containsKey("scaleZ")) line.setScaleZ(((Number) map.get("scaleZ")).floatValue());
        if (map.containsKey("backgroundColor")) {
            int argb = ((Number) map.get("backgroundColor")).intValue();
            line.setBackgroundColor(Color.fromARGB(argb));
        }
        if (map.containsKey("shadow")) line.setShadow((Boolean) map.get("shadow"));
        if (map.containsKey("seeThrough")) line.setSeeThrough((Boolean) map.get("seeThrough"));
        if (map.containsKey("alignment")) {
            try {
                line.setAlignment(TextDisplay.TextAlignment.valueOf((String) map.get("alignment")));
            } catch (Exception ignored) {}
        }
        return line;
    }
}
