package com.zenxy.hologram.protocol;

import com.zenxy.hologram.model.HologramData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface ProtocolAdapter {

    String getProviderName();

    int spawnTextLine(Player player, HologramData hologram, int lineIndex, String formattedText, int entityId);

    void updateTextLine(Player player, int entityId, String formattedText);

    void teleportLine(Player player, int entityId, Location location);

    void destroyEntities(Player player, Collection<Integer> entityIds);

    int spawnInteractionEntity(Player player, HologramData hologram, int entityId);
}
