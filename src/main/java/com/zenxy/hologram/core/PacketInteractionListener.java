package com.zenxy.hologram.core;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.zenxy.hologram.model.HologramData;
import com.zenxy.hologram.model.HologramView;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public class PacketInteractionListener extends PacketListenerAbstract {

    private final JavaPlugin plugin;
    private final HologramManager hologramManager;
    private final HologramRenderer renderer;

    public PacketInteractionListener(JavaPlugin plugin, HologramManager hologramManager, HologramRenderer renderer) {
        this.plugin = plugin;
        this.hologramManager = hologramManager;
        this.renderer = renderer;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            int targetEntityId = packet.getEntityId();
            Player player = (Player) event.getPlayer();

            if (player == null || !player.isOnline()) return;

            UUID uuid = player.getUniqueId();
            Map<String, HologramView> views = renderer.getPlayerViews().get(uuid);

            if (views == null || views.isEmpty()) return;

            for (HologramView view : views.values()) {
                if (view.getAllEntityIds().contains(targetEntityId)) {
                    HologramData hologram = hologramManager.getHologram(view.getHologramId());
                    if (hologram != null && !hologram.getClickActions().isEmpty()) {

                        // CRITICAL THREAD SAFETY: Netty I/O Thread -> Bukkit Main Thread Dispatch
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            executeActions(player, hologram);
                        });
                        break;
                    }
                }
            }
        }
    }

    private void executeActions(Player player, HologramData hologram) {
        for (String action : hologram.getClickActions()) {
            if (action.startsWith("[console] ")) {
                String cmd = action.substring(10).replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            } else if (action.startsWith("[player] ")) {
                String cmd = action.substring(9).replace("%player%", player.getName());
                player.performCommand(cmd);
            } else if (action.startsWith("[message] ")) {
                String msg = action.substring(10).replace("%player%", player.getName());
                player.sendMessage(com.zenxy.hologram.util.ColorUtil.parse(msg));
            }
        }
    }
}
