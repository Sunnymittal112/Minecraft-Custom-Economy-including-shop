package net.fetal.titaneconomy.listeners;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.managers.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class NoteListener implements Listener {

    private final TitanEconomy plugin;
    private final EconomyManager economyManager;

    public NoteListener(TitanEconomy plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
    }

    @EventHandler
    public void onNoteUse(PlayerInteractEvent event) {
        // Basic Checks
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return; // Only Main Hand

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.PAPER) return;

        // --- SECURITY CHECK ---
        // Check karte hain ki kya isme hamara secret key hai?
        NamespacedKey key = new NamespacedKey(plugin, "banknote-value");
        
        if (!item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.DOUBLE)) {
            // Yeh normal paper hai, Bank Note nahi
            return;
        }

        // Security Passed: Get Value
        double amount = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);

        // --- TRANSACTION ---
        // 1. Remove 1 Item
        item.setAmount(item.getAmount() - 1); // Stack mein se 1 kam karo

        // 2. Add Money
        economyManager.deposit(player, amount);

        // 3. Feedback
        String symbol = plugin.getConfig().getString("settings.currency-symbol", "$");
        player.sendMessage(Component.text("Deposited: ", NamedTextColor.GREEN)
                .append(Component.text(symbol + String.format("%,.2f", amount), NamedTextColor.GOLD)));
        
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

        // Update Scoreboard
        plugin.getScoreboardManager().updateScoreboard(player);
    }
}