package com.zenxy.hologram.storage;

import com.zenxy.hologram.model.HologramData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YamlStorage implements StorageAdapter {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public YamlStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "holograms.yml");
    }

    @Override
    public void init() {
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create holograms.yml file!");
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void saveHologram(HologramData hologram) {
        config.set("holograms." + hologram.getId(), hologram.serialize());
        saveFile();
    }

    @Override
    public void saveAllHolograms(Collection<HologramData> holograms) {
        config.set("holograms", null);
        for (HologramData hologram : holograms) {
            config.set("holograms." + hologram.getId(), hologram.serialize());
        }
        saveFile();
    }

    @Override
    public void deleteHologram(String hologramId) {
        config.set("holograms." + hologramId, null);
        saveFile();
    }

    @Override
    public Collection<HologramData> loadAllHolograms() {
        List<HologramData> list = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("holograms");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Map<String, Object> map = section.getConfigurationSection(key).getValues(true);
                try {
                    HologramData data = HologramData.deserialize(map);
                    list.add(data);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load hologram: " + key);
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    @Override
    public void close() {
        saveFile();
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save holograms.yml!");
            e.printStackTrace();
        }
    }
}
