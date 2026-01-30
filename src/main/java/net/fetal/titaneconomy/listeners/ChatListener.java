package net.fetal.titaneconomy.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.fetal.titaneconomy.TitanEconomy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final TitanEconomy plugin;

    public ChatListener(TitanEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        
        // Data fetch karo
        int level = plugin.getLevelManager().getLevel(player);
        // Balance ko 'k' format mein dikhate hain (e.g. 1.5k) taaki chat lamba na ho
        double bal = plugin.getEconomyManager().getBalance(player);
        String balFormatted = formatValue(bal);

        // --- FORMAT ---
        // [Lvl 5] [$1.2k] PlayerName: Message
        
        // Level Part (Yellow)
        Component levelPrefix = Component.text("[Lvl " + level + "] ", NamedTextColor.GOLD);
        
        // Money Part (Green)
        Component moneyPrefix = Component.text("[$" + balFormatted + "] ", NamedTextColor.GREEN);
        
        // Name Part (White)
        Component name = Component.text(player.getName() + ": ", NamedTextColor.WHITE);

        // Combine parts
        // Note: Paper API mein hum renderer change karte hain for maximum compatibility
        event.renderer((source, sourceDisplayName, message, viewer) -> 
            levelPrefix
            .append(moneyPrefix)
            .append(name)
            .append(message.color(NamedTextColor.GRAY)) // Message color grey
        );
    }

    // Helper: 1500 -> 1.5k
    private String formatValue(double value) {
        if (value >= 1_000_000) return String.format("%.1fM", value / 1_000_000);
        if (value >= 1_000) return String.format("%.1fk", value / 1_000);
        return String.format("%.0f", value);
    }
}