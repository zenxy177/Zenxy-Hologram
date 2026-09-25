package com.zenxy.hologram;

import com.zenxy.hologram.command.HologramCommand;
import com.zenxy.hologram.config.ConfigManager;
import com.zenxy.hologram.core.HologramManager;
import com.zenxy.hologram.core.HologramRenderer;
import com.zenxy.hologram.core.HologramTicker;
import com.zenxy.hologram.core.InteractionHandler;
import com.zenxy.hologram.integration.PlaceholderManager;
import com.zenxy.hologram.protocol.ProtocolAdapter;
import com.zenxy.hologram.protocol.impl.BukkitDisplayAdapter;
import com.zenxy.hologram.protocol.impl.PacketEventsAdapter;
import com.zenxy.hologram.protocol.impl.ProtocolLibAdapter;
import com.zenxy.hologram.storage.StorageAdapter;
import com.zenxy.hologram.storage.YamlStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ZenxyHologramPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private StorageAdapter storageAdapter;
    private HologramManager hologramManager;
    private ProtocolAdapter protocolAdapter;
    private HologramRenderer hologramRenderer;
    private HologramTicker hologramTicker;
    private PlaceholderManager placeholderManager;

    @Override
    public void onEnable() {
        getLogger().info("=================================================");
        getLogger().info("    ZenxyHologram v" + getDescription().getVersion() + " Initializing...");
        getLogger().info("=================================================");

        // 1. Config & Messages Setup
        configManager = new ConfigManager(this);
        configManager.reload();

        // 2. Storage Setup
        storageAdapter = new YamlStorage(this);
        storageAdapter.init();

        // 3. Hologram Manager
        hologramManager = new HologramManager(this, storageAdapter);
        hologramManager.loadAll();

        // 4. Integrations
        placeholderManager = new PlaceholderManager();
        if (placeholderManager.isPapiEnabled()) {
            getLogger().info("[Hook] PlaceholderAPI successfully hooked!");
        }

        // 5. Protocol Provider Detection
        protocolAdapter = selectProtocolAdapter();
        getLogger().info("[Protocol] Using packet engine: " + protocolAdapter.getProviderName());

        // 6. Hologram Renderer & Ticker
        hologramRenderer = new HologramRenderer(this, hologramManager, protocolAdapter, placeholderManager);
        long cullingInterval = getConfig().getLong("settings.culling-interval-ticks", 10L);
        hologramRenderer.startCullingTask(cullingInterval);

        hologramTicker = new HologramTicker(this, hologramManager, hologramRenderer, placeholderManager);
        long updateInterval = getConfig().getLong("settings.update-interval-ticks", 5L);
        hologramTicker.startUpdateTask(updateInterval);

        // 7. Event & Command Registration
        getServer().getPluginManager().registerEvents(new InteractionHandler(hologramRenderer), this);

        HologramCommand cmd = new HologramCommand(this);
        if (getCommand("zenxyhologram") != null) {
            getCommand("zenxyhologram").setExecutor(cmd);
            getCommand("zenxyhologram").setTabCompleter(cmd);
        }

        getLogger().info("ZenxyHologram standard boot complete!");
    }

    @Override
    public void onDisable() {
        if (hologramRenderer != null) {
            hologramRenderer.despawnAll();
        }
        if (hologramManager != null) {
            hologramManager.saveAll();
        }
        if (storageAdapter != null) {
            storageAdapter.close();
        }
        getLogger().info("ZenxyHologram shutdown complete.");
    }

    public void reloadAll() {
        configManager.reload();
        if (hologramRenderer != null) {
            hologramRenderer.despawnAll();
        }
        hologramManager.loadAll();
    }

    private ProtocolAdapter selectProtocolAdapter() {
        String mode = getConfig().getString("settings.protocol-provider", "AUTO").toUpperCase();

        if ("PACKETEVENTS".equals(mode) || "AUTO".equals(mode)) {
            if (Bukkit.getPluginManager().isPluginEnabled("PacketEvents")) {
                return new PacketEventsAdapter();
            }
        }
        if ("PROTOCOLLIB".equals(mode) || "AUTO".equals(mode)) {
            if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
                return new ProtocolLibAdapter();
            }
        }
        return new BukkitDisplayAdapter(this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public HologramRenderer getHologramRenderer() {
        return hologramRenderer;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
}
