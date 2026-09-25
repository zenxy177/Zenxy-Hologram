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

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
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
