package net.fetal.titaneconomy.managers;

import net.fetal.titaneconomy.TitanEconomy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ShopManager {

    private final TitanEconomy plugin;
    private File shopFile;
    private FileConfiguration shopConfig;
    
    // Caches
    public final HashMap<Integer, String> categorySlots = new HashMap<>();
    public final HashMap<String, HashMap<Integer, String>> menuItemsCache = new HashMap<>();
    
    // NEW: Pending Purchase Cache (Player UUID -> Config Path of Item)
    // Yeh yaad rakhega ki player ne konsa item select kiya hai quantity menu khulne se pehle
    public final HashMap<UUID, String> pendingPurchase = new HashMap<>();

    public ShopManager(TitanEconomy plugin) {
        this.plugin = plugin;
        loadShopConfig();
    }

    private void loadShopConfig() {
        shopFile = new File(plugin.getDataFolder(), "shops.yml");
        if (!shopFile.exists()) {
            plugin.saveResource("shops.yml", false);
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopFile);
    }

    public void openMainMenu(Player player) {
        String title = shopConfig.getString("main-menu.title", "Titan Marketplace");
        int rows = shopConfig.getInt("main-menu.rows", 3);
        
        Inventory inv = Bukkit.createInventory(null, rows * 9, color(title));
        categorySlots.clear();

        ConfigurationSection cats = shopConfig.getConfigurationSection("main-menu.categories");
        if (cats != null) {
            for (String key : cats.getKeys(false)) {
                int slot = cats.getInt(key + ".slot");
                String matName = cats.getString(key + ".material");
                String name = cats.getString(key + ".name");
                List<String> lore = cats.getStringList(key + ".lore");

                inv.setItem(slot, createGuiItem(Material.valueOf(matName), name, lore));
                categorySlots.put(slot, key);
            }
        }
        player.openInventory(inv);
    }

    public void openCategory(Player player, String categoryId) {
        String title = shopConfig.getString("menus." + categoryId + ".title", "Shop");
        Inventory inv = Bukkit.createInventory(null, 54, color(title)); // 54 Slots (Big Page) for more items
        
        menuItemsCache.putIfAbsent(title, new HashMap<>());
        menuItemsCache.get(title).clear();

        ConfigurationSection items = shopConfig.getConfigurationSection("menus." + categoryId + ".items");
        
        if (items != null) {
            for (String key : items.getKeys(false)) {
                try {
                    int slot = items.getInt(key + ".slot");
                    String matName = items.getString(key + ".material");
                    String name = items.getString(key + ".name");
                    double price = items.getDouble(key + ".price");
                    
                    List<String> lore = new ArrayList<>();
                    lore.add("&7Price per unit: &a$" + price);
                    lore.add("");
                    lore.add("&eClick to Select Quantity");

                    ItemStack item = createGuiItem(Material.valueOf(matName), name, lore);
                    inv.setItem(slot, item);
                    
                    menuItemsCache.get(title).put(slot, key);
                    
                } catch (Exception e) {
                    plugin.getLogger().warning("Error loading item: " + key);
                }
            }
        }
        
        ItemStack back = createGuiItem(Material.ARROW, "&cGo Back", List.of("&7Return to Main Menu"));
        inv.setItem(49, back); // Bottom Center

        player.openInventory(inv);
    }

    // --- NEW: QUANTITY SELECTION MENU ---
    public void openQuantityMenu(Player player, String itemPath) {
        // Pending purchase mein save karo
        pendingPurchase.put(player.getUniqueId(), itemPath);

        String itemName = shopConfig.getString(itemPath + ".name", "Item");
        double price = shopConfig.getDouble(itemPath + ".price");
        String matName = shopConfig.getString(itemPath + ".material");
        Material mat = Material.valueOf(matName);

        Inventory inv = Bukkit.createInventory(null, 27, color("&8Select Amount: " + itemName));

        // Option 1: Buy x1
        inv.setItem(11, createGuiItem(mat, "&aBuy x1", List.of(
            "&7Price: &e$" + (price * 1),
            "",
            "&bClick to Buy!"
        )));

        // Option 2: Buy x32
        ItemStack stack32 = createGuiItem(mat, "&aBuy x32", List.of(
            "&7Price: &e$" + (price * 32),
            "",
            "&bClick to Buy!"
        ));
        stack32.setAmount(32);
        inv.setItem(13, stack32);

        // Option 3: Buy x64
        ItemStack stack64 = createGuiItem(mat, "&aBuy x64", List.of(
            "&7Price: &e$" + (price * 64),
            "",
            "&bClick to Buy!"
        ));
        stack64.setAmount(64);
        inv.setItem(15, stack64);

        // Cancel Button
        inv.setItem(22, createGuiItem(Material.BARRIER, "&cCancel", List.of("&7Go back")));

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(color(name));
            List<Component> loreComp = new ArrayList<>();
            if (lore != null) {
                for (String l : lore) loreComp.add(color(l));
            }
            meta.lore(loreComp);
            item.setItemMeta(meta);
        }
        return item;
    }

    public FileConfiguration getConfig() { return shopConfig; }
    private Component color(String s) { return LegacyComponentSerializer.legacyAmpersand().deserialize(s); }
}