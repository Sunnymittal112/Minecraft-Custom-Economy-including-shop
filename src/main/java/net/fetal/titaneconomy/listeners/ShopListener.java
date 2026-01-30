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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = LegacyComponentSerializer.legacyAmpersand().serialize(event.getView().title());
        String mainTitle = shopManager.getConfig().getString("main-menu.title", "Titan Marketplace");

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
                    String itemKey = shopManager.menuItemsCache.get(title).get(slot);
                    String catId = title.replace("Shop: ", "").toLowerCase();
                    // Exception handling for weird titles
                    if(catId.contains("armor")) catId = "armor"; 
                    
                    String path = "menus." + catId + ".items." + itemKey;
                    
                    // OPEN QUANTITY MENU INSTEAD OF BUYING
                    shopManager.openQuantityMenu(player, path);
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
                player.closeInventory(); 
                // Optional: Re-open category, but closing is safer to avoid loops
                return;
            }

            if (!shopManager.pendingPurchase.containsKey(player.getUniqueId())) {
                player.closeInventory();
                return;
            }

            String itemPath = shopManager.pendingPurchase.get(player.getUniqueId());
            double unitPrice = shopManager.getConfig().getDouble(itemPath + ".price");
            String matName = shopManager.getConfig().getString(itemPath + ".material");
            Material mat = Material.valueOf(matName);

            int amount = 0;
            if (slot == 11) amount = 1;
            else if (slot == 13) amount = 32;
            else if (slot == 15) amount = 64;

            if (amount > 0) {
                double finalPrice = unitPrice * amount;

                if (economyManager.getBalance(player) >= finalPrice) {
                    if (hasSpace(player, amount)) {
                        economyManager.withdraw(player, finalPrice);
                        player.getInventory().addItem(new ItemStack(mat, amount));

                        player.sendMessage(Component.text("Bought " + amount + "x ", NamedTextColor.GREEN)
                            .append(Component.text(mat.name(), NamedTextColor.YELLOW))
                            .append(Component.text(" for $" + finalPrice, NamedTextColor.GREEN)));
                        
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
                        plugin.getScoreboardManager().updateScoreboard(player);
                        player.closeInventory();
                    } else {
                        player.sendMessage(Component.text("Inventory Full!", NamedTextColor.RED));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                } else {
                    player.sendMessage(Component.text("Need $" + finalPrice, NamedTextColor.RED));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
            }
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
}