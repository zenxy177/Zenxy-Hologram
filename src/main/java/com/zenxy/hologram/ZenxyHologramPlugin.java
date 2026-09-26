package com.zenxy.hologram;

import com.github.retrooper.packetevents.PacketEvents;
import com.zenxy.hologram.command.HologramCommand;
import com.zenxy.hologram.config.ConfigManager;
import com.zenxy.hologram.core.HologramManager;
import com.zenxy.hologram.core.HologramRenderer;
import com.zenxy.hologram.core.HologramTicker;
import com.zenxy.hologram.core.PacketInteractionListener;
import com.zenxy.hologram.core.PlayerListener;
import com.zenxy.hologram.integration.PlaceholderManager;
import com.zenxy.hologram.protocol.ProtocolAdapter;
import com.zenxy.hologram.protocol.impl.BukkitDisplayAdapter;
import com.zenxy.hologram.protocol.impl.PacketEventsAdapter;
import com.zenxy.hologram.protocol.impl.ProtocolLibAdapter;
import com.zenxy.hologram.storage.StorageAdapter;
import com.zenxy.hologram.storage.YamlStorage;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
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
    private PacketInteractionListener packetInteractionListener;

    @Override
    public void onLoad() {
        // Embedded PacketEvents Initialization on plugin load
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().checkForUpdates(false).bStats(true);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        getLogger().info("=================================================");
        getLogger().info("    ZenxyHologram v" + getDescription().getVersion() + " Initializing...");
        getLogger().info("=================================================");

        // 1. PacketEvents Runtime Init
        PacketEvents.getAPI().init();

        // 2. Config & Messages Setup
        configManager = new ConfigManager(this);
        configManager.reload();

        // 3. Storage Setup (SQLITE or YAML)
        String storageType = getConfig().getString("settings.storage-type", "SQLITE").toUpperCase();
        if ("SQLITE".equals(storageType)) {
            storageAdapter = new com.zenxy.hologram.storage.SQLiteStorage(this);
        } else {
            storageAdapter = new YamlStorage(this);
        }
        storageAdapter.init();

        // 4. Hologram Manager
        hologramManager = new HologramManager(this, storageAdapter);
        hologramManager.loadAll();

        // 5. Integrations
        placeholderManager = new PlaceholderManager();
        if (placeholderManager.isPapiEnabled()) {
            getLogger().info("[Hook] PlaceholderAPI successfully hooked!");
        }

        // 6. Protocol Provider Detection
        protocolAdapter = selectProtocolAdapter();
        getLogger().info("[Protocol] Using packet engine: " + protocolAdapter.getProviderName());

        // 7. Hologram Renderer & Ticker
        hologramRenderer = new HologramRenderer(this, hologramManager, protocolAdapter, placeholderManager);
        long cullingInterval = getConfig().getLong("settings.culling-interval-ticks", 10L);
        hologramRenderer.startCullingTask(cullingInterval);

        hologramTicker = new HologramTicker(this, hologramManager, hologramRenderer, placeholderManager);
        long updateInterval = getConfig().getLong("settings.update-interval-ticks", 5L);
        hologramTicker.startUpdateTask(updateInterval);

        // 8. Event & Packet Listener Registration
        packetInteractionListener = new PacketInteractionListener(this, hologramManager, hologramRenderer);
        PacketEvents.getAPI().getEventManager().registerListener(packetInteractionListener);

        getServer().getPluginManager().registerEvents(new PlayerListener(hologramRenderer, packetInteractionListener), this);

        HologramCommand cmd = new HologramCommand(this);
        if (getCommand("zenxyhologram") != null) {
            getCommand("zenxyhologram").setExecutor(cmd);
            getCommand("zenxyhologram").setTabCompleter(cmd);
        }

        // 9. bStats Metrics Setup
        try {
            int pluginId = 34332;
            Metrics metrics = new Metrics(this, pluginId);
            metrics.addCustomChart(new SimplePie("protocol_engine", () -> protocolAdapter.getProviderName()));
            metrics.addCustomChart(new SimplePie("storage_engine", () -> getConfig().getString("settings.storage-type", "SQLITE")));
            metrics.addCustomChart(new SingleLineChart("total_holograms", () -> hologramManager.getAllHolograms().size()));
            getLogger().info("[Metrics] bStats telemetry metrics initialized.");
        } catch (Exception ignored) {
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

        // Terminate embedded PacketEvents
        PacketEvents.getAPI().terminate();

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

        if ("BUKKIT".equals(mode)) {
            return new BukkitDisplayAdapter(this);
        }
        if ("PROTOCOLLIB".equals(mode) && Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            return new ProtocolLibAdapter();
        }
        return new PacketEventsAdapter();
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
