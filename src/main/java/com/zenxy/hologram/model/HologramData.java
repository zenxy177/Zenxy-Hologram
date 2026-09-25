package com.zenxy.hologram.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class HologramData {

    private final String id;
    private Location location;
    private final List<HologramLineData> lines;
    private double lineSpacing;
    private Display.Billboard billboard;
    private double renderDistance;
    private final List<String> clickActions;

    public HologramData(String id, Location location) {
        this.id = id;
        this.location = location;
        this.lines = new CopyOnWriteArrayList<>();
        this.lineSpacing = 0.3;
        this.billboard = Display.Billboard.CENTER;
        this.renderDistance = 48.0;
        this.clickActions = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location.clone();
    }

    public void setLocation(Location location) {
        this.location = location.clone();
    }

    public List<HologramLineData> getLines() {
        return lines;
    }

    public void addLine(String text) {
        lines.add(new HologramLineData(text));
    }

    public void addLine(HologramLineData line) {
        lines.add(line);
    }

    public void setLine(int index, String text) {
        if (index >= 0 && index < lines.size()) {
            lines.get(index).setText(text);
        }
    }

    public boolean removeLine(int index) {
        if (index >= 0 && index < lines.size()) {
            lines.remove(index);
            return true;
        }
        return false;
    }

    public double getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(double lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    public Display.Billboard getBillboard() {
        return billboard;
    }

    public void setBillboard(Display.Billboard billboard) {
        this.billboard = billboard;
    }

    public double getRenderDistance() {
        return renderDistance;
    }

    public void setRenderDistance(double renderDistance) {
        this.renderDistance = renderDistance;
    }

    public List<String> getClickActions() {
        return clickActions;
    }

    public Location getLineLocation(int lineIndex) {
        // Line 0 is top, subsequent lines offset downwards by lineSpacing
        return location.clone().add(0, - (lineIndex * lineSpacing), 0);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("world", location.getWorld() != null ? location.getWorld().getName() : "world");
        map.put("x", location.getX());
        map.put("y", location.getY());
        map.put("z", location.getZ());
        map.put("yaw", location.getYaw());
        map.put("pitch", location.getPitch());
        map.put("lineSpacing", lineSpacing);
        map.put("billboard", billboard.name());
        map.put("renderDistance", renderDistance);
        map.put("clickActions", clickActions);

        List<Map<String, Object>> serializedLines = new ArrayList<>();
        for (HologramLineData line : lines) {
            serializedLines.add(line.serialize());
        }
        map.put("lines", serializedLines);

        return map;
    }

    @SuppressWarnings("unchecked")
    public static HologramData deserialize(Map<String, Object> map) {
        String id = (String) map.get("id");
        String worldName = (String) map.getOrDefault("world", "world");
        World world = Bukkit.getWorld(worldName);
        double x = ((Number) map.get("x")).doubleValue();
        double y = ((Number) map.get("y")).doubleValue();
        double z = ((Number) map.get("z")).doubleValue();
        float yaw = ((Number) map.getOrDefault("yaw", 0.0f)).floatValue();
        float pitch = ((Number) map.getOrDefault("pitch", 0.0f)).floatValue();

        Location loc = new Location(world, x, y, z, yaw, pitch);
        HologramData hologram = new HologramData(id, loc);

        if (map.containsKey("lineSpacing")) {
            hologram.setLineSpacing(((Number) map.get("lineSpacing")).doubleValue());
        }
        if (map.containsKey("renderDistance")) {
            hologram.setRenderDistance(((Number) map.get("renderDistance")).doubleValue());
        }
        if (map.containsKey("billboard")) {
            try {
                hologram.setBillboard(Display.Billboard.valueOf((String) map.get("billboard")));
            } catch (Exception ignored) {}
        }
        if (map.containsKey("clickActions")) {
            hologram.getClickActions().addAll((List<String>) map.get("clickActions"));
        }

        if (map.containsKey("lines")) {
            List<Map<String, Object>> serializedLines = (List<Map<String, Object>>) map.get("lines");
            for (Map<String, Object> lineMap : serializedLines) {
                hologram.addLine(HologramLineData.deserialize(lineMap));
            }
        }

        return hologram;
    }
}
