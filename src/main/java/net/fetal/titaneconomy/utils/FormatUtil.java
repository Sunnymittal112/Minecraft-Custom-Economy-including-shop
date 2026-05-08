package net.fetal.titaneconomy.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.text.DecimalFormat;
import java.util.Locale;

public class FormatUtil {

    private static final String[] SUFFIXES = {"", "k", "M", "B", "T", "Q"};
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,##0.##");

    /**
     * Formats a double to a readable string like 1.5k, 2.3M, etc.
     */
    public static String formatNumber(double value, boolean formatShort) {
        if (!formatShort || value < 1000) {
            return String.format(Locale.US, "%.2f", value).replace(".00", "");
        }
        
        int index = 0;
        double tmp = value;
        while (tmp >= 1000 && index < SUFFIXES.length - 1) {
            tmp /= 1000;
            index++;
        }
        return FORMATTER.format(tmp) + SUFFIXES[index];
    }

    /**
     * Translates a string with Legacy &ampersand color codes and MiniMessage tags into a Component.
     */
    public static Component color(String message) {
        if (message == null) return Component.empty();
        
        // First, translate legacy & codes into MiniMessage compatible tags, 
        // or just use LegacyComponentSerializer to parse legacy, then append MiniMessage.
        // Paper's MiniMessage is standard. We can do legacy conversion using LegacyComponentSerializer.
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }
}
