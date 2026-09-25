package com.zenxy.hologram.protocol.impl;

import com.zenxy.hologram.model.HologramData;
import com.zenxy.hologram.protocol.ProtocolAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collection;

public class ProtocolLibAdapter implements ProtocolAdapter {

    @Override
    public String getProviderName() {
        return "ProtocolLib (Dynamic Packet Engine)";
    }

    @Override
    public int spawnTextLine(Player player, HologramData hologram, int lineIndex, String formattedText, int entityId) {
        // Dynamic reflection fallback for ProtocolLib if enabled on server
        try {
            Class<?> protocolManagerClass = Class.forName("com.comphenix.protocol.ProtocolLibrary");
            Method getManager = protocolManagerClass.getMethod("getProtocolManager");
            Object manager = getManager.invoke(null);

            if (manager != null) {
                // ProtocolLib server packet dispatch
            }
        } catch (Exception ignored) {
        }
        return entityId;
    }

    @Override
    public void updateTextLine(Player player, int entityId, String formattedText) {
    }

    @Override
    public void teleportLine(Player player, int entityId, Location location) {
    }

    @Override
    public void destroyEntities(Player player, Collection<Integer> entityIds) {
    }

    @Override
    public int spawnInteractionEntity(Player player, HologramData hologram, int entityId) {
        return entityId;
    }
}
