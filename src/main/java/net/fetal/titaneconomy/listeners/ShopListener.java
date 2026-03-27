package net.fetal.titaneconomy.listeners;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.managers.EconomyManager;
import net.fetal.titaneconomy.managers.ShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class ShopListener implements Listener {

    private final TitanEconomy plugin;
    private final ShopManager shopManager;
    private final EconomyManager economyManager;

    public ShopListener(TitanEconomy plugin) {
        this.plugin = plugin;
        this.shopManager = plugin.getShopManager();
        this.economyManager = plugin.getEconomyManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = LegacyComponentSerializer.legacyAmpersand().serialize(event.getView().title());
        String mainTitle = shopManager.getConfig().getString("main-menu.title", "Titan Marketplace");

        if (shopManager.isSellMenu(title)) {
            event.setCancelled(true);

            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }

            int slot = event.getSlot();
            if (slot == shopManager.getSellCancelSlot()) {
                shopManager.clearSellSelection(player.getUniqueId());
                player.closeInventory();
                return;
            }

            if (slot == shopManager.getSellConfirmSlot()) {
                sellSelectedSlots(player);
                return;
            }

            if (shopManager.isSellMenuSlot(slot)) {
                shopManager.toggleSellSelection(player, slot);
                shopManager.refreshSellMenu(player, event.getView().getTopInventory());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.1f);
            }
            return;
        }

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        // --- 1. MAIN MENU ---
        if (title.equals(mainTitle)) {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (shopManager.categorySlots.containsKey(slot)) {
                shopManager.openCategory(player, shopManager.categorySlots.get(slot));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            return;
        }

        // --- 2. SUB MENU (Items List) ---
        if (title.startsWith("Shop:")) {
            event.setCancelled(true);
            int slot = event.getSlot();

            if (event.getCurrentItem().getType() == Material.ARROW) {
                shopManager.openMainMenu(player);
                return;
            }

            if (shopManager.menuItemsCache.containsKey(title)) {
                if (shopManager.menuItemsCache.get(title).containsKey(slot)) {
                    String itemPath = shopManager.menuItemsCache.get(title).get(slot);
                    if (!shopManager.canBuy(itemPath)) {
                        player.sendMessage(Component.text(
                            shopManager.isUnavailable(itemPath)
                                ? "This item is currently unavailable."
                                : "This item cannot be bought from the shop.",
                            NamedTextColor.RED
                        ));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }
                    shopManager.openQuantityMenu(player, itemPath);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                }
            }
            return;
        }

        // --- 3. QUANTITY MENU (Selection) ---
        if (title.startsWith("&8Select Amount:")) {
            event.setCancelled(true);
            int slot = event.getSlot();

            if (slot == 22) { // Cancel Button
                shopManager.pendingPurchase.remove(player.getUniqueId());
                player.closeInventory(); 
                // Optional: Re-open category, but closing is safer to avoid loops
                return;
            }

            if (!shopManager.pendingPurchase.containsKey(player.getUniqueId())) {
                player.closeInventory();
                return;
            }

            String itemPath = shopManager.pendingPurchase.get(player.getUniqueId());
            if (!shopManager.canBuy(itemPath)) {
                shopManager.pendingPurchase.remove(player.getUniqueId());
                player.sendMessage(Component.text(
                    shopManager.isUnavailable(itemPath)
                        ? "This item is currently unavailable."
                        : "This item cannot be bought from the shop.",
                    NamedTextColor.RED
                ));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.closeInventory();
                return;
            }
            double unitPrice = shopManager.getBuyPrice(itemPath);

            int amount = 0;
            if (slot == 11) amount = 1;
            else if (slot == 13) amount = 32;
            else if (slot == 15) amount = 64;

            if (amount > 0) {
                double finalPrice = unitPrice * amount;
                ItemStack purchasedItem = shopManager.createPurchasedItem(itemPath, amount);

                if (economyManager.getBalance(player) >= finalPrice) {
                    if (hasSpace(player, purchasedItem.getAmount())) {
                        economyManager.withdraw(player, finalPrice);
                        player.getInventory().addItem(purchasedItem);
                        shopManager.pendingPurchase.remove(player.getUniqueId());

                        player.sendMessage(Component.text("Bought " + amount + "x ", NamedTextColor.GREEN)
                            .append(Component.text(shopManager.getPlainItemName(itemPath), NamedTextColor.YELLOW))
                            .append(Component.text(" for $" + shopManager.formatPrice(finalPrice), NamedTextColor.GREEN)));
                        
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
                        plugin.getScoreboardManager().updateScoreboard(player);
                        player.closeInventory();
                    } else {
                        player.sendMessage(Component.text("Inventory Full!", NamedTextColor.RED));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                } else {
                    player.sendMessage(Component.text("Need $" + shopManager.formatPrice(finalPrice), NamedTextColor.RED));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = LegacyComponentSerializer.legacyAmpersand().serialize(event.getView().title());
        if (shopManager.isSellMenu(title)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = LegacyComponentSerializer.legacyAmpersand().serialize(event.getView().title());
        if (shopManager.isSellMenu(title)) {
            shopManager.clearSellSelection(event.getPlayer().getUniqueId());
            return;
        }
        if (title.startsWith("&8Select Amount:")) {
            shopManager.pendingPurchase.remove(event.getPlayer().getUniqueId());
        }
    }
    
    // Helper to check inventory space
    private boolean hasSpace(Player player, int amount) {
        int free = 0;
        for (ItemStack i : player.getInventory().getStorageContents()) {
            if (i == null || i.getType() == Material.AIR) {
                free += 64; // Assumption: items stack to 64
            } else if (i.getAmount() < 64) {
                 // Check if same type, add remaining space (Complex logic omitted for simplicity)
            }
        }
        return free >= amount; // Very basic check
    }

    private void sellSelectedSlots(Player player) {
        Set<Integer> selectedSlots = shopManager.getSelectedSellSlots(player.getUniqueId());
        if (selectedSlots.isEmpty()) {
            player.sendMessage(Component.text("Select at least one sellable slot first.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        double total = 0.0D;
        int soldSlots = 0;
        for (int playerSlot : selectedSlots) {
            double slotValue = shopManager.sellInventorySlot(player, playerSlot);
            if (slotValue > 0.0D) {
                total += slotValue;
                soldSlots++;
            }
        }

        shopManager.clearSellSelection(player.getUniqueId());
        if (soldSlots == 0) {
            player.sendMessage(Component.text("Nothing selected could be sold.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.closeInventory();
            return;
        }

        player.sendMessage(Component.text("Sold items from " + soldSlots + " slot(s) for $" + shopManager.formatPrice(total), NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        plugin.getScoreboardManager().updateScoreboard(player);
        player.closeInventory();
    }
}
