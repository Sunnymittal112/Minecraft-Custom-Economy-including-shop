package net.fetal.titaneconomy.listeners;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.managers.AuctionManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class AuctionListener implements Listener {

    private final AuctionManager auctionManager;

    public AuctionListener(TitanEconomy plugin) {
        this.auctionManager = plugin.getAuctionManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = LegacyComponentSerializer.legacyAmpersand().serialize(event.getView().title());
        
        if (title.contains("Global Auction House")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (!(event.getWhoClicked() instanceof Player player)) return;

            // Clicked inside GUI
            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                auctionManager.buyItem(player, event.getSlot());
            }
        }
    }
}