package net.fetal.titaneconomy.commands;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.managers.ShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopCommand implements CommandExecutor {

    private final ShopManager shopManager;

    public ShopCommand(TitanEconomy plugin) {
        this.shopManager = plugin.getShopManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can shop.", NamedTextColor.RED));
            return true;
        }

        // --- FIX HERE ---
        // Purana: shopManager.openShop(player);
        // Naya: shopManager.openMainMenu(player);
        shopManager.openMainMenu(player);
        
        return true;
    }
}