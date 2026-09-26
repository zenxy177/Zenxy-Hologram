package com.zenxy.hologram.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final JavaPlugin plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;
    private final Map<String, String> messagesCache = new HashMap<>();
    private String language = "en-US"; // default language

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        // Load language setting (en-US default)
        this.language = plugin.getConfig().getString("settings.language", "en-US");

        // Choose appropriate messages file based on language
        String messagesFileName = "messages.yml";
        if (!"tr-TR".equalsIgnoreCase(this.language)) {
            messagesFileName = "messages-en-US.yml";
        }
        messagesFile = new File(plugin.getDataFolder(), messagesFileName);
        if (!messagesFile.exists()) {
            // Fallback to default Turkish messages if language file missing
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        messagesCache.clear();
        for (String key : messagesConfig.getKeys(true)) {
            if (messagesConfig.isString(key)) {
                messagesCache.put(key, messagesConfig.getString(key));
            }
        }
    }

    public String getMessage(String key) {
        return messagesCache.getOrDefault(key, messagesConfig != null ? messagesConfig.getString(key, "") : "");
    }
}
