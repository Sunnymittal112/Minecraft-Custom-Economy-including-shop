package net.fetal.titaneconomy.commands;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.managers.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BalanceCommand implements CommandExecutor {

    private final EconomyManager economyManager;

    public BalanceCommand(TitanEconomy plugin) {
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        // Check if sender is a player
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can check balance.", NamedTextColor.RED));
            return true;
        }

        // Get balance
        double bal = economyManager.getBalance(player);
        String currency = TitanEconomy.getInstance().getConfig().getString("settings.currency-symbol", "$");

        // Send Message: "Wallet: $500.00"
        player.sendMessage(Component.text("Wallet: ", NamedTextColor.GRAY)
                .append(Component.text(currency + String.format("%.2f", bal), NamedTextColor.GREEN)));

        return true;
    }
}