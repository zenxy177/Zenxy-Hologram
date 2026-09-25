package com.zenxy.hologram.core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class InteractionHandler implements Listener {

    private final HologramRenderer renderer;

    public InteractionHandler(HologramRenderer renderer) {
        this.renderer = renderer;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        renderer.despawnAllForPlayer(event.getPlayer());
    }
}
