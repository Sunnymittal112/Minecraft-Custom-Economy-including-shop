package net.fetal.titaneconomy.commands;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.managers.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WithdrawCommand implements CommandExecutor {

    private final TitanEconomy plugin;
    private final EconomyManager economyManager;

    public WithdrawCommand(TitanEconomy plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can withdraw notes.", NamedTextColor.RED));
            return true;
        }

        // Usage: /withdraw <amount>
        if (args.length != 1) {
            player.sendMessage(Component.text("Usage: /withdraw <amount>", NamedTextColor.RED));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid number.", NamedTextColor.RED));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(Component.text("Amount must be positive.", NamedTextColor.RED));
            return true;
        }

        // Balance Check
        if (economyManager.getBalance(player) < amount) {
            player.sendMessage(Component.text("Insufficient funds!", NamedTextColor.RED));
            return true;
        }

        // Inventory Check
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(Component.text("Inventory full! Clear a slot.", NamedTextColor.RED));
            return true;
        }

        // --- TRANSACTION ---
        // 1. Withdraw Money
        economyManager.withdraw(player, amount);

        // 2. Create Bank Note
        ItemStack note = new ItemStack(Material.PAPER);
        ItemMeta meta = note.getItemMeta();
        
        String symbol = plugin.getConfig().getString("settings.currency-symbol", "$");
        String formattedAmount = String.format("%,.2f", amount); // e.g., "1,000.00"

        // Set Name
        meta.displayName(Component.text(symbol + formattedAmount + " Bank Note", NamedTextColor.GREEN, TextDecoration.BOLD));

        // Set Lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Value: ", NamedTextColor.GRAY).append(Component.text(symbol + formattedAmount, NamedTextColor.WHITE)));
        lore.add(Component.text("Signer: ", NamedTextColor.GRAY).append(Component.text(player.getName(), NamedTextColor.YELLOW)));
        lore.add(Component.text(""));
        lore.add(Component.text("(Right Click to Deposit)", NamedTextColor.GOLD, TextDecoration.ITALIC));
        meta.lore(lore);

        // --- SECURITY LAYER (PDC) ---
        // Hum amount ko item ke hidden data mein save karenge
        NamespacedKey key = new NamespacedKey(plugin, "banknote-value");
        meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, amount);

        // --- VISUAL GLINT (Enchantment glow without enchant) ---
        // Modern paper mein glint add karna thoda complex hai, simple rakhte hain.
        // Agar chahiye toh "Enchantment.LUCK" add karke HideFlag laga sakte hain.
        
        note.setItemMeta(meta);

        // 3. Give Item
        player.getInventory().addItem(note);
        
        player.sendMessage(Component.text("Withdrew ", NamedTextColor.GREEN)
                .append(Component.text(symbol + formattedAmount, NamedTextColor.GOLD)));

        // Update Scoreboard
        plugin.getScoreboardManager().updateScoreboard(player);

        return true;
    }
}