package com.zenxy.hologram.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zenxy.hologram.model.HologramData;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLiteStorage implements StorageAdapter {

    private final JavaPlugin plugin;
    private final File dbFile;
    private final Gson gson;
    private final ExecutorService dbExecutor;
    private Connection connection;

    public SQLiteStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "holograms.db");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dbExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "ZenxyHologram-DB-Worker"));
    }

    @Override
    public void init() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            // WAL Mode & Busy Timeout to prevent "database is locked" errors
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA busy_timeout=5000;");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS zenxy_holograms (" +
                        "id VARCHAR(64) PRIMARY KEY, " +
                        "data TEXT NOT NULL" +
                        ");");
            }
            plugin.getLogger().info("SQLite storage initialized with WAL mode & busy timeout!");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize SQLite storage!");
            e.printStackTrace();
        }
    }

    @Override
    public void saveHologram(HologramData hologram) {
        dbExecutor.submit(() -> {
            if (connection == null) return;
            String sql = "INSERT OR REPLACE INTO zenxy_holograms (id, data) VALUES (?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, hologram.getId().toLowerCase());
                pstmt.setString(2, gson.toJson(hologram.serialize()));
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error saving hologram to SQLite: " + hologram.getId());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void saveAllHolograms(Collection<HologramData> holograms) {
        dbExecutor.submit(() -> {
            if (connection == null) return;
            String sql = "INSERT OR REPLACE INTO zenxy_holograms (id, data) VALUES (?, ?)";
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    for (HologramData hologram : holograms) {
                        pstmt.setString(1, hologram.getId().toLowerCase());
                        pstmt.setString(2, gson.toJson(hologram.serialize()));
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                plugin.getLogger().severe("Error batch saving holograms to SQLite!");
                e.printStackTrace();
            }
        });
    }

    @Override
    public void deleteHologram(String hologramId) {
        dbExecutor.submit(() -> {
            if (connection == null) return;
            String sql = "DELETE FROM zenxy_holograms WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, hologramId.toLowerCase());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error deleting hologram from SQLite: " + hologramId);
                e.printStackTrace();
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<HologramData> loadAllHolograms() {
        // Synchronous read during plugin startup
        List<HologramData> list = new ArrayList<>();
        if (connection == null) return list;

        String sql = "SELECT data FROM zenxy_holograms";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String json = rs.getString("data");
                Map<String, Object> map = gson.fromJson(json, Map.class);
                HologramData hologram = HologramData.deserialize(map);
                list.add(hologram);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error loading holograms from SQLite!");
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void close() {
        dbExecutor.shutdown();
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
