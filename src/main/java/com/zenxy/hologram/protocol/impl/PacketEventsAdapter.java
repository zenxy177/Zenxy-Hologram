package com.zenxy.hologram.protocol.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.zenxy.hologram.model.HologramData;
import com.zenxy.hologram.model.HologramLineData;
import com.zenxy.hologram.protocol.ProtocolAdapter;
import com.zenxy.hologram.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PacketEventsAdapter implements ProtocolAdapter {

    @Override
    public String getProviderName() {
        return "PacketEvents (Packet-Based Virtual Render)";
    }

    @Override
    public int spawnTextLine(Player player, HologramData hologram, int lineIndex, String formattedText, int entityId) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user == null) return entityId;

        Location loc = hologram.getLineLocation(lineIndex);
        HologramLineData lineData = hologram.getLines().get(lineIndex);

        // 1. Spawn Packet
        com.github.retrooper.packetevents.protocol.world.Location peLoc = new com.github.retrooper.packetevents.protocol.world.Location(
                loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()
        );

        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                entityId,
                UUID.randomUUID(),
                EntityTypes.TEXT_DISPLAY,
                peLoc,
                loc.getYaw(),
                0,
                null
        );

        user.sendPacket(spawnPacket);

        // 2. Metadata Packet (TextDisplay Properties)
        sendTextDisplayMetadata(user, entityId, formattedText, lineData, hologram.getBillboard());

        return entityId;
    }

    @Override
    public void updateTextLine(Player player, int entityId, String formattedText) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user == null) return;

        Component component = ColorUtil.parse(formattedText);
        List<EntityData> entityDataList = new ArrayList<>();
        // Index 23: TextDisplay Component
        entityDataList.add(new EntityData(23, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(component)));

        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(entityId, entityDataList);
        user.sendPacket(metadataPacket);
    }

    private void sendTextDisplayMetadata(User user, int entityId, String formattedText, HologramLineData lineData, Display.Billboard billboard) {
        List<EntityData> entityDataList = new ArrayList<>();

        // Index 15: Billboard constraints (0 = FIXED, 1 = VERTICAL, 2 = HORIZONTAL, 3 = CENTER)
        byte billboardByte = switch (billboard) {
            case FIXED -> (byte) 0;
            case VERTICAL -> (byte) 1;
            case HORIZONTAL -> (byte) 2;
            default -> (byte) 3; // CENTER
        };
        entityDataList.add(new EntityData(15, EntityDataTypes.BYTE, billboardByte));

        // Index 23: Text Component
        Component component = ColorUtil.parse(formattedText);
        entityDataList.add(new EntityData(23, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(component)));

        // Index 25: Background Color ARGB
        if (lineData.getBackgroundColor() != null) {
            entityDataList.add(new EntityData(25, EntityDataTypes.INT, lineData.getBackgroundColor().asARGB()));
        }

        // Index 27: Display Flags (Bitmask: 0x01 = Shadow, 0x02 = SeeThrough)
        byte flags = 0;
        if (lineData.isShadow()) flags |= 0x01;
        if (lineData.isSeeThrough()) flags |= 0x02;
        entityDataList.add(new EntityData(27, EntityDataTypes.BYTE, flags));

        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(entityId, entityDataList);
        user.sendPacket(metadataPacket);
    }

    @Override
    public void teleportLine(Player player, int entityId, Location location) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user == null) return;

        com.github.retrooper.packetevents.protocol.world.Location peLoc = new com.github.retrooper.packetevents.protocol.world.Location(
                location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()
        );

        WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(
                entityId,
                peLoc,
                false
        );

        user.sendPacket(teleportPacket);
    }

    @Override
    public void destroyEntities(Player player, Collection<Integer> entityIds) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user == null || entityIds.isEmpty()) return;

        int[] ids = entityIds.stream().mapToInt(Integer::intValue).toArray();
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(ids);
        user.sendPacket(destroyPacket);
    }

    @Override
    public int spawnInteractionEntity(Player player, HologramData hologram, int entityId) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user == null) return entityId;

        Location loc = hologram.getLocation();
        com.github.retrooper.packetevents.protocol.world.Location peLoc = new com.github.retrooper.packetevents.protocol.world.Location(
                loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()
        );

        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                entityId,
                UUID.randomUUID(),
                EntityTypes.INTERACTION,
                peLoc,
                loc.getYaw(),
                0,
                null
        );

        user.sendPacket(spawnPacket);
        return entityId;
    }
}
