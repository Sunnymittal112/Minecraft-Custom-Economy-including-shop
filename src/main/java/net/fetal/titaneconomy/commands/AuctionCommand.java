package net.fetal.titaneconomy.commands;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.managers.AuctionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AuctionCommand implements CommandExecutor {

    private final AuctionManager auctionManager;

    public AuctionCommand(TitanEconomy plugin) {
        this.auctionManager = plugin.getAuctionManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Players only.", NamedTextColor.RED));
            return true;
        }

        // /ah -> Open GUI
        if (args.length == 0) {
            auctionManager.openAuctionHouse(player);
            return true;
        }

        // /ah sell <price>
        if (args[0].equalsIgnoreCase("sell")) {
            if (args.length != 2) {
                player.sendMessage(Component.text("Usage: /ah sell <price>", NamedTextColor.RED));
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage(Component.text("You must hold an item!", NamedTextColor.RED));
                return true;
            }

            double price;
            try {
                price = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid price.", NamedTextColor.RED));
                return true;
            }

            if (price <= 0) {
                player.sendMessage(Component.text("Price must be positive.", NamedTextColor.RED));
                return true;
            }

            // List Item
            auctionManager.listItem(player, item, price);
            player.getInventory().setItemInMainHand(null); // Remove item
            player.sendMessage(Component.text("Item listed for $" + price, NamedTextColor.GREEN));
            return true;
        }

        return true;
    }
}