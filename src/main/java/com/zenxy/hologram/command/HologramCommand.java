package com.zenxy.hologram.command;

import com.zenxy.hologram.ZenxyHologramPlugin;
import com.zenxy.hologram.model.HologramData;
import com.zenxy.hologram.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class HologramCommand implements CommandExecutor, TabCompleter {

    private final ZenxyHologramPlugin plugin;

    public HologramCommand(ZenxyHologramPlugin plugin) {
        this.plugin = plugin;
    }

    private void sendMessage(CommandSender sender, String key, Map<String, String> replacements) {
        String msg = plugin.getConfigManager().getMessage(key);
        if (msg == null || msg.isEmpty()) return;
        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                msg = msg.replace("<" + entry.getKey() + ">", entry.getValue());
            }
        }
        String prefix = plugin.getConfigManager().getMessage("prefix");
        Component parsed = ColorUtil.parse((prefix != null ? prefix : "") + msg);
        sender.sendMessage(parsed);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("zenxyhologram.admin")) {
            sendMessage(sender, "no-permission", null);
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Bu komut sadece oyuncular içindir.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.parse("<red>Kullanım: /zholo create <isim></red>"));
                    return true;
                }
                String id = args[1];
                if (plugin.getHologramManager().getHologram(id) != null) {
                    sendMessage(player, "hologram-already-exists", Map.of("name", id));
                    return true;
                }
                HologramData hologram = plugin.getHologramManager().createHologram(id, player.getLocation());
                hologram.addLine("&e&lZenxy Hologram &7- Yeni Hologram");
                hologram.addLine("&7Düzenlemek için: &f/zholo setline " + id + " 0 <metin>");
                plugin.getHologramManager().saveHologram(hologram);
                plugin.getHologramRenderer().refreshHologram(hologram);
                sendMessage(player, "created", Map.of("name", id));
                break;
            }

            case "delete":
            case "remove": {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.parse("<red>Kullanım: /zholo delete <isim></red>"));
                    return true;
                }
                String id = args[1];
                HologramData h = plugin.getHologramManager().getHologram(id);
                if (h != null) {
                    plugin.getHologramRenderer().refreshHologram(h);
                    plugin.getHologramManager().deleteHologram(id);
                    sendMessage(sender, "deleted", Map.of("name", id));
                } else {
                    sendMessage(sender, "hologram-not-found", Map.of("name", id));
                }
                break;
            }

            case "list": {
                Collection<HologramData> all = plugin.getHologramManager().getAllHolograms();
                sender.sendMessage(ColorUtil.parse("<gradient:#FF5555:#FFAA00>&lMevcut Hologramlar (" + all.size() + "):</gradient>"));
                for (HologramData h : all) {
                    sender.sendMessage(ColorUtil.parse(" &8- &e" + h.getId() + " &7(Satır: " + h.getLines().size() + ", Dünya: " + (h.getLocation().getWorld() != null ? h.getLocation().getWorld().getName() : "null") + ")"));
                }
                break;
            }

            case "near": {
                if (!(sender instanceof Player player)) return true;
                double r = 15.0;
                if (args.length >= 2) {
                    try {
                        r = Double.parseDouble(args[1]);
                        if (r <= 0) {
                            player.sendMessage(ColorUtil.parse("<red>Radyus sıfırdan büyük bir sayı olmalıdır!</red>"));
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ColorUtil.parse("<red>Geçersiz radyus değeri! Lütfen bir sayı giriniz.</red>"));
                        return true;
                    }
                }
                final double radius = r;
                Location pLoc = player.getLocation();
                List<HologramData> nearby = plugin.getHologramManager().getAllHolograms().stream()
                        .filter(h -> h.getLocation().getWorld() != null && h.getLocation().getWorld().equals(pLoc.getWorld()))
                        .filter(h -> h.getLocation().distanceSquared(pLoc) <= (radius * radius))
                        .toList();

                player.sendMessage(ColorUtil.parse("<gradient:#FF5555:#FFAA00>&lYakındaki Hologramlar (" + nearby.size() + "):</gradient>"));
                for (HologramData h : nearby) {
                    double dist = Math.round(Math.sqrt(h.getLocation().distanceSquared(pLoc)) * 10.0) / 10.0;
                    player.sendMessage(ColorUtil.parse(" &8- &e" + h.getId() + " &7(" + dist + "m uzaklıkta)"));
                }
                break;
            }

            case "tp":
            case "teleport": {
                if (!(sender instanceof Player player)) return true;
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.parse("<red>Kullanım: /zholo tp <isim></red>"));
                    return true;
                }
                String id = args[1];
                HologramData h = plugin.getHologramManager().getHologram(id);
                if (h == null) {
                    sendMessage(player, "hologram-not-found", Map.of("name", id));
                    return true;
                }
                player.teleport(h.getLocation());
                sendMessage(player, "teleported", Map.of("name", id));
                break;
            }

            case "movehere": {
                if (!(sender instanceof Player player)) return true;
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.parse("<red>Kullanım: /zholo movehere <isim></red>"));
                    return true;
                }
                String id = args[1];
                HologramData h = plugin.getHologramManager().getHologram(id);
                if (h == null) {
                    sendMessage(player, "hologram-not-found", Map.of("name", id));
                    return true;
                }
                h.setLocation(player.getLocation());
                plugin.getHologramManager().saveHologram(h);
                plugin.getHologramRenderer().refreshHologram(h);
                sendMessage(player, "moved", Map.of("name", id));
                break;
            }

            case "addline": {
                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.parse("<red>Kullanım: /zholo addline <isim> <metin></red>"));
                    return true;
                }
                String id = args[1];
                HologramData h = plugin.getHologramManager().getHologram(id);
                if (h == null) {
                    sendMessage(sender, "hologram-not-found", Map.of("name", id));
                    return true;
                }
                String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                h.addLine(text);
                int newIndex = h.getLines().size() - 1;
                plugin.getHologramManager().saveHologram(h);
                plugin.getHologramRenderer().refreshHologram(h);
                sendMessage(sender, "line-added", Map.of("name", id, "index", String.valueOf(newIndex), "text", text));
                break;
            }

            case "setline": {
                if (args.length < 4) {
                    sender.sendMessage(ColorUtil.parse("<red>Kullanım: /zholo setline <isim> <index> <metin></red>"));
                    return true;
                }
                String id = args[1];
                HologramData h = plugin.getHologramManager().getHologram(id);
                if (h == null) {
                    sendMessage(sender, "hologram-not-found", Map.of("name", id));
                    return true;
                }
                try {
                    int idx = Integer.parseInt(args[2]);
                    if (idx < 0 || idx >= h.getLines().size()) {
                        sendMessage(sender, "invalid-line-index", Map.of("max", String.valueOf(h.getLines().size())));
                        return true;
                    }
                    String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    h.setLine(idx, text);
                    plugin.getHologramManager().saveHologram(h);
                    plugin.getHologramRenderer().refreshHologram(h);
                    sendMessage(sender, "line-set", Map.of("name", id, "index", String.valueOf(idx), "text", text));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.parse("<red>İndex bir sayı olmalıdır!</red>"));
                }
                break;
            }

            case "removeline": {
                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.parse("<red>Kullanım: /zholo removeline <isim> <index></red>"));
                    return true;
                }
                String id = args[1];
                HologramData h = plugin.getHologramManager().getHologram(id);
                if (h == null) {
                    sendMessage(sender, "hologram-not-found", Map.of("name", id));
                    return true;
                }
                try {
                    int idx = Integer.parseInt(args[2]);
                    if (h.removeLine(idx)) {
                        plugin.getHologramManager().saveHologram(h);
                        plugin.getHologramRenderer().refreshHologram(h);
                        sendMessage(sender, "line-removed", Map.of("name", id, "index", String.valueOf(idx)));
                    } else {
                        sendMessage(sender, "invalid-line-index", Map.of("max", String.valueOf(h.getLines().size())));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.parse("<red>İndex bir sayı olmalıdır!</red>"));
                }
                break;
            }

            case "setscale": {
                if (args.length < 4) {
                    sender.sendMessage(ColorUtil.parse("<red>Kullanım: /zholo setscale <isim> <index> <scale></red>"));
                    return true;
                }
                String id = args[1];
                HologramData h = plugin.getHologramManager().getHologram(id);
                if (h == null) {
                    sendMessage(sender, "hologram-not-found", Map.of("name", id));
                    return true;
                }
                try {
                    int idx = Integer.parseInt(args[2]);
                    float scale = Float.parseFloat(args[3]);
                    if (idx >= 0 && idx < h.getLines().size()) {
                        h.getLines().get(idx).setUniformScale(scale);
                        plugin.getHologramManager().saveHologram(h);
                        plugin.getHologramRenderer().refreshHologram(h);
                        sendMessage(sender, "scale-set", Map.of("name", id, "index", String.valueOf(idx), "scale", String.valueOf(scale)));
                    } else {
                        sendMessage(sender, "invalid-line-index", Map.of("max", String.valueOf(h.getLines().size())));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.parse("<red>Geçersiz sayı formatı!</red>"));
                }
                break;
            }

            case "setbillboard": {
                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.parse("<red>Kullanım: /zholo setbillboard <isim> <CENTER|FIXED|VERTICAL|HORIZONTAL></red>"));
                    return true;
                }
                String id = args[1];
                HologramData h = plugin.getHologramManager().getHologram(id);
                if (h == null) {
                    sendMessage(sender, "hologram-not-found", Map.of("name", id));
                    return true;
                }
                try {
                    Display.Billboard bb = Display.Billboard.valueOf(args[2].toUpperCase());
                    h.setBillboard(bb);
                    plugin.getHologramManager().saveHologram(h);
                    plugin.getHologramRenderer().refreshHologram(h);
                    sendMessage(sender, "billboard-set", Map.of("name", id, "billboard", bb.name()));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ColorUtil.parse("<red>Geçersiz billboard modu! (CENTER, FIXED, VERTICAL, HORIZONTAL)</red>"));
                }
                break;
            }

            case "reload": {
                plugin.reloadAll();
                sendMessage(sender, "reload-success", null);
                break;
            }

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("help-header")));
        sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("help-create")));
        sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("help-delete")));
        sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("help-list")));
        sender.sendMessage(ColorUtil.parse("<yellow>/zholo near [radyus]</yellow> - Yakındaki hologramları gösterir"));
        sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("help-tp")));
        sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("help-move")));
        sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("help-addline")));
        sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("help-setline")));
        sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("help-removeline")));
        sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("help-setscale")));
        sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("help-reload")));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(List.of("create", "delete", "list", "near", "tp", "movehere", "addline", "setline", "removeline", "setscale", "setbillboard", "reload"), args[0]);
        }
        if (args.length == 2 && List.of("delete", "tp", "movehere", "addline", "setline", "removeline", "setscale", "setbillboard").contains(args[0].toLowerCase())) {
            return filter(plugin.getHologramManager().getAllHolograms().stream().map(HologramData::getId).toList(), args[1]);
        }
        if (args.length == 3 && List.of("setline", "removeline", "setscale").contains(args[0].toLowerCase())) {
            HologramData h = plugin.getHologramManager().getHologram(args[1]);
            if (h != null) {
                List<String> indices = new ArrayList<>();
                for (int i = 0; i < h.getLines().size(); i++) indices.add(String.valueOf(i));
                return filter(indices, args[2]);
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("setbillboard")) {
            return filter(List.of("CENTER", "FIXED", "VERTICAL", "HORIZONTAL"), args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String input) {
        String lower = input.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(lower)).toList();
    }
}
