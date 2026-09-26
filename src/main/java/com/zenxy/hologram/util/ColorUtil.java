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
     * Translates legacy color codes (&a, &#RRGGBB) and MiniMessage syntax (<gradient:...>, <rainbow>, <red>, <b>)
     * into a rich Adventure Component.
     */
    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        // 1. Try MiniMessage parsing if input contains XML-like tags
        if (input.contains("<") && input.contains(">")) {
            try {
                String normalized = normalizeLegacyInMiniMessage(input);
                return MINI_MESSAGE.deserialize(normalized);
            } catch (Exception ignored) {
            }
        }

        // 2. Legacy Ampersand & Hex Parsing (&#RRGGBB, &a, &l)
        String hexFormatted = parseHexColors(input);
        return LEGACY_SERIALIZER.deserialize(hexFormatted);
    }

    public static String colorize(String input) {
        if (input == null) return "";
        String hexParsed = parseHexColors(input);
        return ChatColor.translateAlternateColorCodes('&', hexParsed);
    }

    private static String normalizeLegacyInMiniMessage(String text) {
        // Convert common legacy formatting tags if used inside MiniMessage tags
        return text.replace("&l", "<b>")
                .replace("&o", "<i>")
                .replace("&n", "<u>")
                .replace("&m", "<st>")
                .replace("&k", "<obf>")
                .replace("&r", "<reset>");
    }

    private static String parseHexColors(String text) {
        if (!text.contains("&#")) return text;
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
