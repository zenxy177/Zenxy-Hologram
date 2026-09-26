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
import java.util.concurrent.ConcurrentHashMap;

public class PacketInteractionListener extends PacketListenerAbstract {

    private final JavaPlugin plugin;
    private final HologramManager hologramManager;
    private final HologramRenderer renderer;
    private final Map<UUID, Long> clickCooldown = new ConcurrentHashMap<>();

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
            long now = System.currentTimeMillis();

            // 1. Rate Limiting / Debounce (Minimum 300 ms aralık)
            Long lastClick = clickCooldown.get(uuid);
            if (lastClick != null && (now - lastClick) < 300L) {
                return;
            }
            clickCooldown.put(uuid, now);

            Map<String, HologramView> views = renderer.getPlayerViews().get(uuid);
            if (views == null || views.isEmpty()) return;

            for (HologramView view : views.values()) {
                if (view.getAllEntityIds().contains(targetEntityId)) {
                    HologramData hologram = hologramManager.getHologram(view.getHologramId());
                    if (hologram != null && !hologram.getClickActions().isEmpty()) {

                        // 2. Netty Network Thread -> Bukkit Main Thread (Paper AsyncCatcher Fix)
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
        if (!player.isOnline()) return;
        for (String action : hologram.getClickActions()) {
            String lower = action.toLowerCase();
            if (lower.startsWith("[console] ")) {
                String cmd = sanitizeCommand(action.substring(10).replace("%player%", player.getName()));
                if (cmd.startsWith("/")) cmd = cmd.substring(1);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            } else if (lower.startsWith("[player] ")) {
                String cmd = sanitizeCommand(action.substring(9).replace("%player%", player.getName()));
                if (cmd.startsWith("/")) cmd = cmd.substring(1);
                player.performCommand(cmd);
            } else if (lower.startsWith("[message] ")) {
                String msg = action.substring(10).replace("%player%", player.getName());
                player.sendMessage(com.zenxy.hologram.util.ColorUtil.parse(msg));
            } else if (lower.startsWith("[sound] ")) {
                String soundName = action.substring(8).trim().toUpperCase();
                try {
                    org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundName);
                    player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                } catch (Exception ignored) {
                }
            } else if (lower.startsWith("[server] ")) {
                String server = action.substring(9).trim();
                player.performCommand("server " + server);
            } else {
                // Default fallback: if no prefix, execute as player command
                String cmd = sanitizeCommand(action.replace("%player%", player.getName()));
                if (cmd.startsWith("/")) cmd = cmd.substring(1);
                player.performCommand(cmd);
            }
        }
    }

    public void removeCooldown(UUID uuid) {
        clickCooldown.remove(uuid);
    }

    private String sanitizeCommand(String command) {
        if (command == null) return "";
        return command.replace("\r", "").replace("\n", "").replace("\0", "").trim();
    }
}
