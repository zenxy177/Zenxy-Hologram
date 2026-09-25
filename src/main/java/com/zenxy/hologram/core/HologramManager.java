package com.zenxy.hologram.core;

import com.zenxy.hologram.model.HologramData;
import com.zenxy.hologram.storage.StorageAdapter;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class HologramManager {

    private final JavaPlugin plugin;
    private final StorageAdapter storage;
    private final Map<String, HologramData> hologramMap = new ConcurrentHashMap<>();
    
    // Spatial Chunk Index: WorldName -> (ChunkKey -> Set of Holograms)
    private final Map<String, Map<Long, Set<HologramData>>> spatialChunkIndex = new ConcurrentHashMap<>();

    public HologramManager(JavaPlugin plugin, StorageAdapter storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public static long getChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX & 0xFFFFFFFFL) | (((long) chunkZ & 0xFFFFFFFFL) << 32);
    }

    public static long getChunkKey(Location loc) {
        return getChunkKey(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public void loadAll() {
        hologramMap.clear();
        spatialChunkIndex.clear();
        Collection<HologramData> loaded = storage.loadAllHolograms();
        for (HologramData holo : loaded) {
            hologramMap.put(holo.getId().toLowerCase(), holo);
            indexHologram(holo);
        }
        plugin.getLogger().info("Loaded and spatially indexed " + hologramMap.size() + " holograms.");
    }

    private void indexHologram(HologramData hologram) {
        Location loc = hologram.getLocation();
        if (loc.getWorld() == null) return;

        String worldName = loc.getWorld().getName();
        long chunkKey = getChunkKey(loc);

        spatialChunkIndex
                .computeIfAbsent(worldName, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(chunkKey, k -> new CopyOnWriteArraySet<>())
                .add(hologram);
    }

    private void deindexHologram(HologramData hologram) {
        Location loc = hologram.getLocation();
        if (loc.getWorld() == null) return;

        String worldName = loc.getWorld().getName();
        long chunkKey = getChunkKey(loc);

        Map<Long, Set<HologramData>> worldMap = spatialChunkIndex.get(worldName);
        if (worldMap != null) {
            Set<HologramData> set = worldMap.get(chunkKey);
            if (set != null) {
                set.remove(hologram);
            }
        }
    }

    public HologramData createHologram(String id, Location location) {
        String key = id.toLowerCase();
        if (hologramMap.containsKey(key)) {
            return null;
        }
        HologramData hologram = new HologramData(id, location);
        hologramMap.put(key, hologram);
        indexHologram(hologram);
        storage.saveHologram(hologram);
        return hologram;
    }

    public boolean deleteHologram(String id) {
        String key = id.toLowerCase();
        HologramData removed = hologramMap.remove(key);
        if (removed != null) {
            deindexHologram(removed);
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

    /**
     * Fast $O(K)$ spatial lookup querying only holograms within nearby chunk radius.
     */
    public Set<HologramData> getNearbyHolograms(Location center, double radiusBlocks) {
        if (center.getWorld() == null) return Collections.emptySet();

        String worldName = center.getWorld().getName();
        Map<Long, Set<HologramData>> worldMap = spatialChunkIndex.get(worldName);
        if (worldMap == null || worldMap.isEmpty()) return Collections.emptySet();

        int centerChunkX = center.getBlockX() >> 4;
        int centerChunkZ = center.getBlockZ() >> 4;
        int chunkRadius = ((int) Math.ceil(radiusBlocks)) >> 4 + 1;

        Set<HologramData> nearby = new HashSet<>();

        for (int cx = centerChunkX - chunkRadius; cx <= centerChunkX + chunkRadius; cx++) {
            for (int cz = centerChunkZ - chunkRadius; cz <= centerChunkZ + chunkRadius; cz++) {
                long key = getChunkKey(cx, cz);
                Set<HologramData> set = worldMap.get(key);
                if (set != null) {
                    nearby.addAll(set);
                }
            }
        }
        return nearby;
    }

    public void saveHologram(HologramData hologram) {
        storage.saveHologram(hologram);
    }

    public void saveAll() {
        storage.saveAllHolograms(hologramMap.values());
    }
}
