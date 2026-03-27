package net.fetal.titaneconomy.commands;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.managers.ShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SellHandCommand implements CommandExecutor {

    private final ShopManager shopManager;
    private final TitanEconomy plugin;

    public SellHandCommand(TitanEconomy plugin) {
        this.plugin = plugin;
        this.shopManager = plugin.getShopManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can sell items.", NamedTextColor.RED));
            return true;
        }

        int heldSlot = player.getInventory().getHeldItemSlot();
        if (player.getInventory().getItem(heldSlot) == null || player.getInventory().getItem(heldSlot).getType() == Material.AIR) {
            player.sendMessage(Component.text("Hold an item in your main hand first.", NamedTextColor.RED));
            return true;
        }
        if (!shopManager.isSellable(player.getInventory().getItem(heldSlot))) {
            player.sendMessage(Component.text("This item cannot be sold to the shop.", NamedTextColor.RED));
            return true;
        }

        int amount = player.getInventory().getItem(heldSlot).getAmount();
        String itemName = shopManager.getPlainItemName(player.getInventory().getItem(heldSlot));
        double total = shopManager.sellInventorySlot(player, heldSlot);
        if (total <= 0.0D) {
            player.sendMessage(Component.text("This item cannot be sold to the shop.", NamedTextColor.RED));
            return true;
        }

        player.sendMessage(Component.text("Sold " + amount + "x ", NamedTextColor.GREEN)
                .append(Component.text(itemName, NamedTextColor.YELLOW))
                .append(Component.text(" for $" + shopManager.formatPrice(total), NamedTextColor.GREEN)));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        plugin.getScoreboardManager().updateScoreboard(player);
        return true;
    }
}
