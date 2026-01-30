package net.fetal.titaneconomy.commands;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.managers.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PayCommand implements CommandExecutor {

    private final EconomyManager economyManager;

    public PayCommand(TitanEconomy plugin) {
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        // Usage: /pay <player> <amount>
        if (args.length != 2) {
            player.sendMessage(Component.text("Usage: /pay <player> <amount>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(Component.text("You cannot pay yourself!", NamedTextColor.RED));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid amount.", NamedTextColor.RED));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(Component.text("Amount must be positive.", NamedTextColor.RED));
            return true;
        }

        if (economyManager.getBalance(player) < amount) {
            player.sendMessage(Component.text("Insufficient funds!", NamedTextColor.RED));
            return true;
        }

        // Transaction
        economyManager.withdraw(player, amount);
        economyManager.deposit(target, amount);

        // Success Messages
        player.sendMessage(Component.text("You sent $" + amount + " to " + target.getName(), NamedTextColor.GREEN));
        target.sendMessage(Component.text("You received $" + amount + " from " + player.getName(), NamedTextColor.GREEN));

        // Update Scoreboards
        TitanEconomy.getInstance().getScoreboardManager().updateScoreboard(player);
        TitanEconomy.getInstance().getScoreboardManager().updateScoreboard(target);

        return true;
    }
}