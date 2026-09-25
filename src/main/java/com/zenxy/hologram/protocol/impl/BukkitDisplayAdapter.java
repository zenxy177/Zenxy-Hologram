package com.zenxy.hologram.protocol.impl;

import com.zenxy.hologram.model.HologramData;
import com.zenxy.hologram.model.HologramLineData;
import com.zenxy.hologram.protocol.ProtocolAdapter;
import com.zenxy.hologram.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitDisplayAdapter implements ProtocolAdapter {

    private final JavaPlugin plugin;
    private final Map<Integer, Entity> spawnedEntities = new ConcurrentHashMap<>();

    public BukkitDisplayAdapter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getProviderName() {
        return "Bukkit (Paper TextDisplay API)";
    }

    @Override
    public int spawnTextLine(Player player, HologramData hologram, int lineIndex, String formattedText, int entityId) {
        Location loc = hologram.getLineLocation(lineIndex);
        HologramLineData lineData = hologram.getLines().get(lineIndex);

        // Run on main thread for Bukkit Entity Spawning
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!loc.isWorldLoaded()) return;
            TextDisplay display = loc.getWorld().spawn(loc, TextDisplay.class, entity -> {
                entity.text(ColorUtil.parse(formattedText));
                entity.setBillboard(hologram.getBillboard());
                entity.setShadowed(lineData.isShadow());
                entity.setSeeThrough(lineData.isSeeThrough());
                entity.setAlignment(lineData.getAlignment());
                if (lineData.getBackgroundColor() != null) {
                    entity.setBackgroundColor(lineData.getBackgroundColor());
                }
                entity.setPersistent(false);
            });
            spawnedEntities.put(display.getEntityId(), display);
        });
        return entityId;
    }

    @Override
    public void updateTextLine(Player player, int entityId, String formattedText) {
        Entity entity = spawnedEntities.get(entityId);
        if (entity instanceof TextDisplay display) {
            Bukkit.getScheduler().runTask(plugin, () -> display.text(ColorUtil.parse(formattedText)));
        }
    }

    @Override
    public void teleportLine(Player player, int entityId, Location location) {
        Entity entity = spawnedEntities.get(entityId);
        if (entity != null) {
            Bukkit.getScheduler().runTask(plugin, () -> entity.teleport(location));
        }
    }

    @Override
    public void destroyEntities(Player player, Collection<Integer> entityIds) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (int id : entityIds) {
                Entity entity = spawnedEntities.remove(id);
                if (entity != null && entity.isValid()) {
                    entity.remove();
                }
            }
        });
    }

    @Override
    public int spawnInteractionEntity(Player player, HologramData hologram, int entityId) {
        return entityId;
    }
}
