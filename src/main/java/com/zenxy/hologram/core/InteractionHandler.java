package com.zenxy.hologram.core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class InteractionHandler implements Listener {

    private final HologramRenderer renderer;

    public InteractionHandler(HologramRenderer renderer) {
        this.renderer = renderer;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Complete memory cleanup when player leaves server
        renderer.despawnAllForPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        // Clear old world hologram views
        renderer.despawnAllForPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld() != null && !event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            renderer.despawnAllForPlayer(event.getPlayer());
        }
    }
}
