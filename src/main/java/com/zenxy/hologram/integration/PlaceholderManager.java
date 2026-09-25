package com.zenxy.hologram.integration;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderManager {

    private final boolean papiEnabled;

    public PlaceholderManager() {
        this.papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public boolean isPapiEnabled() {
        return papiEnabled;
    }

    /**
     * Parses PlaceholderAPI placeholders if installed, otherwise returns text as-is.
     */
    public String setPlaceholders(Player player, String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (papiEnabled && player != null) {
            try {
                return PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception e) {
                return text;
            }
        }
        return text;
    }
}
