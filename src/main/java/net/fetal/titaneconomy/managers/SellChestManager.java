package net.fetal.titaneconomy.managers;

import net.fetal.titaneconomy.TitanEconomy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.TileState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class SellChestManager {
    private static final String SHOP_ITEM_PATH = "menus.misc.items.sell_chest";
    private static final double DEFAULT_INTERVAL_MINUTES = 3.0D;
    private static final List<BlockFace> CHEST_NEIGHBOR_FACES = List.of(
        BlockFace.NORTH,
        BlockFace.SOUTH,
        BlockFace.EAST,
        BlockFace.WEST
    );

    private final TitanEconomy plugin;
    private final NamespacedKey sellChestItemKey;
    private final NamespacedKey sellChestBlockKey;
    private final NamespacedKey sellChestOwnerKey;
    private final HashMap<String, UUID> registeredChests = new HashMap<>();

    private File dataFile;
    private FileConfiguration dataConfig;
    private BukkitTask autoSellTask;

    public SellChestManager(TitanEconomy plugin) {
        this.plugin = plugin;
        this.sellChestItemKey = new NamespacedKey(plugin, "sell_chest_item");
        this.sellChestBlockKey = new NamespacedKey(plugin, "sell_chest_block");
        this.sellChestOwnerKey = new NamespacedKey(plugin, "sell_chest_owner");
        loadData();
        startAutoSellTask();
    }

    public void shutdown() {
        if (autoSellTask != null) {
            autoSellTask.cancel();
            autoSellTask = null;
        }
        saveData();
    }

    public String getShopItemPath() {
        return SHOP_ITEM_PATH;
    }

    public boolean isSellChestItemPath(String itemPath) {
        return SHOP_ITEM_PATH.equals(itemPath);
    }

    public String getIntervalDescription() {
        double minutes = plugin.getConfig().getDouble("settings.sell-chest-interval-minutes", DEFAULT_INTERVAL_MINUTES);
        if (minutes <= 0.0D) {
            minutes = DEFAULT_INTERVAL_MINUTES;
        }

        double roundedMinutes = Math.rint(minutes);
        if (Math.abs(minutes - roundedMinutes) < 0.0001D) {
            int wholeMinutes = (int) roundedMinutes;
            return wholeMinutes + (wholeMinutes == 1 ? " minute" : " minutes");
        }

        double seconds = minutes * 60.0D;
        double roundedSeconds = Math.rint(seconds);
        if (Math.abs(seconds - roundedSeconds) < 0.0001D) {
            int wholeSeconds = (int) roundedSeconds;
            return wholeSeconds + (wholeSeconds == 1 ? " second" : " seconds");
        }

        return String.format(Locale.US, "%.2f minutes", minutes);
    }

    public ItemStack createSellChestItem(int amount, String configuredName) {
        ItemStack item = new ItemStack(Material.CHEST, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(color(configuredName != null ? configuredName : "&6Auto Sell Chest"));
            List<Component> lore = new ArrayList<>();
            lore.add(color("&7Acts like a normal chest."));
            lore.add(color("&7Auto-sells contents every " + getIntervalDescription() + "."));
            lore.add(color("&7Only works while the chunk is loaded."));
            lore.add(color(""));
            lore.add(color("&ePlace to bind it to you."));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(sellChestItemKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createDroppedSellChest() {
        String displayName = plugin.getShopManager().getConfiguredItemName(SHOP_ITEM_PATH);
        return createSellChestItem(1, displayName);
    }

    public boolean isSellChestItem(ItemStack item) {
        if (item == null || item.getType() != Material.CHEST) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        return meta.getPersistentDataContainer().has(sellChestItemKey, PersistentDataType.BYTE);
    }

    public boolean isSellChestBlock(Block block) {
        if (!(block.getState() instanceof TileState tileState)) {
            return false;
        }
        return tileState.getPersistentDataContainer().has(sellChestBlockKey, PersistentDataType.BYTE);
    }

    public boolean isAdjacentToChest(Block block) {
        for (BlockFace face : CHEST_NEIGHBOR_FACES) {
            if (block.getRelative(face).getType() == Material.CHEST) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdjacentToSellChest(Block block) {
        for (BlockFace face : CHEST_NEIGHBOR_FACES) {
            if (isSellChestBlock(block.getRelative(face))) {
                return true;
            }
        }
        return false;
    }

    public void registerSellChest(Player owner, Block block) {
        if (!(block.getState() instanceof TileState tileState)) {
            return;
        }

        tileState.getPersistentDataContainer().set(sellChestBlockKey, PersistentDataType.BYTE, (byte) 1);
        tileState.getPersistentDataContainer().set(sellChestOwnerKey, PersistentDataType.STRING, owner.getUniqueId().toString());
        tileState.update();

        registeredChests.put(locationKey(block.getLocation()), owner.getUniqueId());
        saveData();
    }

    public void unregisterSellChest(Block block) {
        boolean updated = registeredChests.remove(locationKey(block.getLocation())) != null;

        if (block.getState() instanceof TileState tileState) {
            if (tileState.getPersistentDataContainer().has(sellChestBlockKey, PersistentDataType.BYTE)) {
                tileState.getPersistentDataContainer().remove(sellChestBlockKey);
                tileState.getPersistentDataContainer().remove(sellChestOwnerKey);
                tileState.update();
                updated = true;
            }
        }

        if (updated) {
            saveData();
        }
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "sellchests.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create sellchests.yml: " + e.getMessage());
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        registeredChests.clear();

        ConfigurationSection section = dataConfig.getConfigurationSection("sell-chests");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            String ownerString = section.getString(key);
            if (ownerString == null) {
                continue;
            }

            try {
                registeredChests.put(key, UUID.fromString(ownerString));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Skipping invalid sell chest owner for " + key);
            }
        }
    }

    private void saveData() {
        dataConfig.set("sell-chests", null);
        for (Map.Entry<String, UUID> entry : registeredChests.entrySet()) {
            dataConfig.set("sell-chests." + entry.getKey(), entry.getValue().toString());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save sellchests.yml: " + e.getMessage());
        }
    }

    private void startAutoSellTask() {
        long intervalTicks = Math.max(20L, Math.round(plugin.getConfig().getDouble("settings.sell-chest-interval-minutes", DEFAULT_INTERVAL_MINUTES) * 60.0D * 20.0D));
        autoSellTask = Bukkit.getScheduler().runTaskTimer(plugin, this::processSellChests, intervalTicks, intervalTicks);
    }

    private void processSellChests() {
        if (registeredChests.isEmpty()) {
            return;
        }

        boolean updated = false;
        List<String> staleKeys = new ArrayList<>();
        HashMap<UUID, SaleSummary> ownerSummaries = new HashMap<>();

        for (Map.Entry<String, UUID> entry : new HashMap<>(registeredChests).entrySet()) {
            Location location = locationFromKey(entry.getKey());
            if (location == null || location.getWorld() == null) {
                staleKeys.add(entry.getKey());
                continue;
            }

            World world = location.getWorld();
            if (!world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                continue;
            }

            Block block = world.getBlockAt(location);
            if (block.getType() != Material.CHEST || !isSellChestBlock(block)) {
                staleKeys.add(entry.getKey());
                continue;
            }

            SaleSummary chestSales = sellChestContents(block);
            if (chestSales.hasSales()) {
                ownerSummaries.computeIfAbsent(entry.getValue(), key -> new SaleSummary()).merge(chestSales);
            }
        }

        for (String staleKey : staleKeys) {
            if (registeredChests.remove(staleKey) != null) {
                updated = true;
            }
        }

        if (updated) {
            saveData();
        }

        for (Map.Entry<UUID, SaleSummary> entry : ownerSummaries.entrySet()) {
            SaleSummary summary = entry.getValue();
            if (!summary.hasSales()) {
                continue;
            }

            OfflinePlayer owner = Bukkit.getOfflinePlayer(entry.getKey());
            plugin.getEconomyManager().deposit(owner, summary.total);

            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                sendSaleSummary(player, summary);
                plugin.getScoreboardManager().updateScoreboard(player);
            }
        }
    }

    private SaleSummary sellChestContents(Block block) {
        Chest chest = (Chest) block.getState();
        Inventory inventory = chest.getBlockInventory();
        SaleSummary summary = new SaleSummary();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (!plugin.getShopManager().isSellable(item)) {
                continue;
            }

            double sellValue = plugin.getShopManager().getSellValue(item);
            if (sellValue <= 0.0D) {
                continue;
            }

            summary.add(plugin.getShopManager().getPlainItemName(item), item.getAmount(), sellValue);
            inventory.setItem(slot, null);
        }

        return summary;
    }

    private void sendSaleSummary(Player player, SaleSummary summary) {
        player.sendMessage(Component.text("Auto Sell Chest sold:", NamedTextColor.GOLD));
        for (SoldItem item : summary.items.values()) {
            player.sendMessage(Component.text("- " + item.amount + "x " + item.name + " for $" + plugin.getShopManager().formatPrice(item.total), NamedTextColor.YELLOW));
        }
        player.sendMessage(Component.text("Total: $" + plugin.getShopManager().formatPrice(summary.total), NamedTextColor.GREEN));
    }

    private String locationKey(Location location) {
        return String.format(
            Locale.US,
            "%s;%d;%d;%d",
            location.getWorld().getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ()
        );
    }

    private Location locationFromKey(String key) {
        String[] parts = key.split(";");
        if (parts.length != 4) {
            return null;
        }

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }

        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Component color(String value) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(value);
    }

    private static final class SaleSummary {
        private final LinkedHashMap<String, SoldItem> items = new LinkedHashMap<>();
        private double total;

        private void add(String name, int amount, double value) {
            SoldItem soldItem = items.computeIfAbsent(name, key -> new SoldItem(name));
            soldItem.amount += amount;
            soldItem.total += value;
            total += value;
        }

        private void merge(SaleSummary other) {
            for (SoldItem item : other.items.values()) {
                add(item.name, item.amount, item.total);
            }
        }

        private boolean hasSales() {
            return total > 0.0D;
        }
    }

    private static final class SoldItem {
        private final String name;
        private int amount;
        private double total;

        private SoldItem(String name) {
            this.name = name;
        }
    }
}
