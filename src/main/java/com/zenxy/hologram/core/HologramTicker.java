package com.zenxy.hologram.core;

import com.zenxy.hologram.integration.PlaceholderManager;
import com.zenxy.hologram.model.HologramData;
import com.zenxy.hologram.model.HologramView;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public class HologramTicker {

    private final JavaPlugin plugin;
    private final HologramManager hologramManager;
    private final HologramRenderer renderer;
    private final PlaceholderManager placeholderManager;

    public HologramTicker(JavaPlugin plugin, HologramManager hologramManager, HologramRenderer renderer, PlaceholderManager placeholderManager) {
        this.plugin = plugin;
        this.hologramManager = hologramManager;
        this.renderer = renderer;
        this.placeholderManager = placeholderManager;
    }

    public void startUpdateTask(long intervalTicks) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::tickUpdates, 20L, intervalTicks);
    }

    private void tickUpdates() {
        for (Map.Entry<UUID, Map<String, HologramView>> entry : renderer.getPlayerViews().entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;

            Map<String, HologramView> views = entry.getValue();

            for (HologramView view : views.values()) {
                HologramData hologram = hologramManager.getHologram(view.getHologramId());
                if (hologram == null) continue;

                for (int i = 0; i < hologram.getLines().size(); i++) {
                    Integer entityId = view.getLineEntityId(i);
                    if (entityId == null) continue;

                    String rawText = hologram.getLines().get(i).getText();
                    
                    // Only process placeholder updates if text contains placeholders or animation tags
                    if (rawText.contains("%") || rawText.contains("<")) {
                        String parsedText = placeholderManager.setPlaceholders(player, rawText);
                        String lastSent = view.getLastSentText(i);

                        // Only send packet if text actually changed (minimizes packet bandwidth)
                        if (!parsedText.equals(lastSent)) {
                            renderer.getProtocolAdapter().updateTextLine(player, entityId, parsedText);
                            view.setLastSentText(i, parsedText);
                        }
                    }
                }
            }
        }
    }
}
