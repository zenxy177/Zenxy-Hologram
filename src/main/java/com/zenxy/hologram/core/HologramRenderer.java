package com.zenxy.hologram.core;

import com.zenxy.hologram.integration.PlaceholderManager;
import com.zenxy.hologram.model.HologramData;
import com.zenxy.hologram.model.HologramView;
import com.zenxy.hologram.protocol.ProtocolAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HologramRenderer {

    private final JavaPlugin plugin;
    private final HologramManager hologramManager;
    private final ProtocolAdapter protocolAdapter;
    private final PlaceholderManager placeholderManager;

    // Entity ID Generator for virtual entities (Negative IDs guarantee 0% collision with Minecraft server entities)
    private final AtomicInteger idGenerator = new AtomicInteger(-1000000);

    // Active views per player: Player UUID -> (Hologram ID -> HologramView)
    private final Map<UUID, Map<String, HologramView>> playerViews = new ConcurrentHashMap<>();

    public HologramRenderer(JavaPlugin plugin, HologramManager hologramManager, ProtocolAdapter protocolAdapter, PlaceholderManager placeholderManager) {
        this.plugin = plugin;
        this.hologramManager = hologramManager;
        this.protocolAdapter = protocolAdapter;
        this.placeholderManager = placeholderManager;
    }

    public ProtocolAdapter getProtocolAdapter() {
        return protocolAdapter;
    }

    public void startCullingTask(long intervalTicks) {
        // Runs on main Bukkit thread to guarantee 100% thread safety when reading Player & World locations
        Bukkit.getScheduler().runTaskTimer(plugin, this::tickCulling, 10L, intervalTicks);
    }

    /**
     * Evaluates distance between online players and holograms, spawning or destroying client-side entities.
     */
    private void tickCulling() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isOnline()) continue;

            UUID uuid = player.getUniqueId();
            Map<String, HologramView> views = playerViews.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
            Location pLoc = player.getLocation();

            Set<HologramData> nearbyHolograms = hologramManager.getNearbyHolograms(pLoc, 64.0);

            for (HologramData hologram : nearbyHolograms) {
                Location hLoc = hologram.getLocation();

                boolean sameWorld = hLoc.getWorld() != null && hLoc.getWorld().equals(pLoc.getWorld());
                boolean inRange = sameWorld && hLoc.distanceSquared(pLoc) <= (hologram.getRenderDistance() * hologram.getRenderDistance());

                HologramView existingView = views.get(hologram.getId());

                if (inRange) {
                    if (existingView == null) {
                        // Player entered render distance -> Spawn Hologram
                        spawnHologramForPlayer(player, hologram, views);
                    }
                } else {
                    if (existingView != null) {
                        // Player exited render distance -> Despawn Hologram
                        despawnHologramForPlayer(player, hologram.getId(), views);
                    }
                }
            }
        }
    }

    public void spawnHologramForPlayer(Player player, HologramData hologram, Map<String, HologramView> views) {
        HologramView view = new HologramView(player.getUniqueId(), hologram.getId());

        for (int i = 0; i < hologram.getLines().size(); i++) {
            int entityId = idGenerator.getAndIncrement();
            String text = hologram.getLines().get(i).getText();
            String parsedText = placeholderManager.setPlaceholders(player, text);

            protocolAdapter.spawnTextLine(player, hologram, i, parsedText, entityId);
            view.setLineEntityId(i, entityId);
            view.setLastSentText(i, parsedText);
        }

        // Spawn interaction entity if click actions are present
        if (!hologram.getClickActions().isEmpty()) {
            int interactionId = idGenerator.getAndIncrement();
            protocolAdapter.spawnInteractionEntity(player, hologram, interactionId);
            view.setInteractionEntityId(interactionId);
        }

        views.put(hologram.getId(), view);
    }

    public void despawnHologramForPlayer(Player player, String hologramId, Map<String, HologramView> views) {
        HologramView view = views.remove(hologramId);
        if (view != null) {
            protocolAdapter.destroyEntities(player, view.getAllEntityIds());
        }
    }

    /**
     * Instantly refreshes a hologram for all active viewers upon modification.
     */
    public void refreshHologram(HologramData hologram) {
        for (Map.Entry<UUID, Map<String, HologramView>> entry : playerViews.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                Map<String, HologramView> views = entry.getValue();
                if (views.containsKey(hologram.getId())) {
                    despawnHologramForPlayer(player, hologram.getId(), views);
                    spawnHologramForPlayer(player, hologram, views);
                }
            }
        }
    }

    public void despawnAllForPlayer(Player player) {
        Map<String, HologramView> views = playerViews.remove(player.getUniqueId());
        if (views != null) {
            for (HologramView view : views.values()) {
                protocolAdapter.destroyEntities(player, view.getAllEntityIds());
            }
        }
    }

    public void despawnAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            despawnAllForPlayer(player);
        }
        playerViews.clear();
    }

    public Map<UUID, Map<String, HologramView>> getPlayerViews() {
        return playerViews;
    }
}
