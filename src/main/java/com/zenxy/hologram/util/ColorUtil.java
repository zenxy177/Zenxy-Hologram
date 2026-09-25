package com.zenxy.hologram.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Translates legacy color codes (&a, &#RRGGBB) and MiniMessage syntax (<red>, <gradient:...>) into Component.
     */
    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        // First convert &#RRGGBB to legacy ChatColor format if present
        String hexFormatted = parseHexColors(input);
        
        // Translate legacy ampersand format to Component
        Component legacyComponent = LEGACY_SERIALIZER.deserialize(hexFormatted);

        // Also attempt parsing with MiniMessage if tags exist
        if (input.contains("<") && input.contains(">")) {
            try {
                return MINI_MESSAGE.deserialize(input);
            } catch (Exception ignored) {
            }
        }
        return legacyComponent;
    }

    /**
     * Converts formatted text to legacy string (& / § format) for older packet serializers or log outputs.
     */
    public static String colorize(String input) {
        if (input == null) return "";
        String hexParsed = parseHexColors(input);
        return ChatColor.translateAlternateColorCodes('&', hexParsed);
    }

    private static String parseHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder builder = new StringBuilder(text.length());
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hexCode.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(builder, replacement.toString());
        }
        matcher.appendTail(builder);
        return builder.toString();
    }
}
