package com.zenxy.hologram.core;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {

    private final HologramRenderer renderer;
    private final PacketInteractionListener packetInteractionListener;

    public PlayerListener(HologramRenderer renderer, PacketInteractionListener packetInteractionListener) {
        this.renderer = renderer;
        this.packetInteractionListener = packetInteractionListener;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // 1. Despawn all virtual entities and remove HologramView tracking from memory
        renderer.despawnAllForPlayer(player);
        // 2. Remove rate-limit cooldown tracking from memory
        if (packetInteractionListener != null) {
            packetInteractionListener.removeCooldown(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        // Clear all virtual hologram entity views from old world
        renderer.despawnAllForPlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld() != null && !event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            renderer.despawnAllForPlayer(event.getPlayer());
        }
    }
}
