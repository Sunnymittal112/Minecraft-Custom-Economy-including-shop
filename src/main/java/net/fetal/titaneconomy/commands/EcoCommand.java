package net.fetal.titaneconomy.commands;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.managers.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EcoCommand implements CommandExecutor {

    private final EconomyManager economyManager;

    public EcoCommand(TitanEconomy plugin) {
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        // Permission Check
        if (!sender.hasPermission("titaneconomy.admin")) {
            sender.sendMessage(Component.text("You do not have permission!", NamedTextColor.RED));
            return true;
        }

        // Usage: /eco <give|take|set> <player> <amount>
        if (args.length != 3) {
            sender.sendMessage(Component.text("Usage: /eco <give|take|set> <player> <amount>", NamedTextColor.RED));
            return true;
        }

        String action = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]); // Offline support zaroori hai admin ke liye
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid amount.", NamedTextColor.RED));
            return true;
        }

        if (!economyManager.hasAccount(target)) {
            economyManager.createAccount(target);
        }

        switch (action) {
            case "give":
                economyManager.deposit(target, amount);
                sender.sendMessage(Component.text("Gave $" + amount + " to " + target.getName(), NamedTextColor.GREEN));
                break;
            case "take":
                economyManager.withdraw(target, amount);
                sender.sendMessage(Component.text("Took $" + amount + " from " + target.getName(), NamedTextColor.YELLOW));
                break;
            case "set":
                economyManager.setBalance(target, amount);
                sender.sendMessage(Component.text("Set " + target.getName() + "'s balance to $" + amount, NamedTextColor.AQUA));
                break;
            default:
                sender.sendMessage(Component.text("Unknown action. Use give, take, or set.", NamedTextColor.RED));
                return true;
        }

        // Update Target's Scoreboard if they are online
        if (target.isOnline() && target.getPlayer() != null) {
            TitanEconomy.getInstance().getScoreboardManager().updateScoreboard(target.getPlayer());
        }

        return true;
    }
}