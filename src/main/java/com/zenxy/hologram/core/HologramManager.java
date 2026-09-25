package com.zenxy.hologram.core;

import com.zenxy.hologram.model.HologramData;
import com.zenxy.hologram.storage.StorageAdapter;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager {

    private final JavaPlugin plugin;
    private final StorageAdapter storage;
    private final Map<String, HologramData> hologramMap = new ConcurrentHashMap<>();

    public HologramManager(JavaPlugin plugin, StorageAdapter storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void loadAll() {
        hologramMap.clear();
        Collection<HologramData> loaded = storage.loadAllHolograms();
        for (HologramData holo : loaded) {
            hologramMap.put(holo.getId().toLowerCase(), holo);
        }
        plugin.getLogger().info("Loaded " + hologramMap.size() + " holograms from storage.");
    }

    public HologramData createHologram(String id, Location location) {
        String key = id.toLowerCase();
        if (hologramMap.containsKey(key)) {
            return null;
        }
        HologramData hologram = new HologramData(id, location);
        hologramMap.put(key, hologram);
        storage.saveHologram(hologram);
        return hologram;
    }

    public boolean deleteHologram(String id) {
        String key = id.toLowerCase();
        HologramData removed = hologramMap.remove(key);
        if (removed != null) {
            storage.deleteHologram(removed.getId());
            return true;
        }
        return false;
    }

    public HologramData getHologram(String id) {
        return hologramMap.get(id.toLowerCase());
    }

    public Collection<HologramData> getAllHolograms() {
        return Collections.unmodifiableCollection(hologramMap.values());
    }

    public void saveHologram(HologramData hologram) {
        storage.saveHologram(hologram);
    }

    public void saveAll() {
        storage.saveAllHolograms(hologramMap.values());
    }
}
