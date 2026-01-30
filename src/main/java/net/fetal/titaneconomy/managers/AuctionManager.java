package net.fetal.titaneconomy.managers;

import net.fetal.titaneconomy.TitanEconomy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class AuctionManager {

    private final TitanEconomy plugin;
    private final List<AuctionItem> auctions = new ArrayList<>();
    private File auctionFile;
    private FileConfiguration auctionConfig;

    public AuctionManager(TitanEconomy plugin) {
        this.plugin = plugin;
        loadAuctions();
    }

    // --- LISTING ITEM ---
    public void listItem(Player player, ItemStack item, double price) {
        AuctionItem auction = new AuctionItem(player.getUniqueId(), item.clone(), price, System.currentTimeMillis());
        auctions.add(auction);
        saveAuctions();
    }

    // --- OPEN GUI ---
    public void openAuctionHouse(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("Global Auction House"));
        
        // Add items to GUI
        int slot = 0;
        for (AuctionItem auction : auctions) {
            if (slot >= 54) break; // Page limit (Simplified)

            ItemStack guiItem = auction.item.clone();
            ItemMeta meta = guiItem.getItemMeta();
            List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
            
            OfflinePlayer seller = Bukkit.getOfflinePlayer(auction.sellerUUID);
            String sellerName = (seller.getName() != null) ? seller.getName() : "Unknown";

            lore.add(Component.text(""));
            lore.add(color("&7Seller: &e" + sellerName));
            lore.add(color("&7Price: &a$" + String.format("%.2f", auction.price)));
            lore.add(color(""));
            
            if (seller.getUniqueId().equals(player.getUniqueId())) {
                lore.add(color("&cClick to Cancel Listing"));
            } else {
                lore.add(color("&eClick to Buy!"));
            }
            
            meta.lore(lore);
            guiItem.setItemMeta(meta);
            
            inv.setItem(slot, guiItem);
            slot++;
        }
        player.openInventory(inv);
    }

    // --- BUY LOGIC ---
    public void buyItem(Player buyer, int slot) {
        if (slot >= auctions.size()) return;

        AuctionItem auction = auctions.get(slot);
        
        // Self-Buy Check (Cancel)
        if (auction.sellerUUID.equals(buyer.getUniqueId())) {
            buyer.getInventory().addItem(auction.item);
            auctions.remove(slot);
            saveAuctions();
            buyer.sendMessage(color("&cListing cancelled. Item returned."));
            openAuctionHouse(buyer); // Refresh
            return;
        }

        // Money Check
        if (plugin.getEconomyManager().getBalance(buyer) >= auction.price) {
            // Inventory Space Check
            if (buyer.getInventory().firstEmpty() == -1) {
                buyer.sendMessage(color("&cInventory Full!"));
                return;
            }

            // Transaction
            plugin.getEconomyManager().withdraw(buyer, auction.price);
            plugin.getEconomyManager().deposit(Bukkit.getOfflinePlayer(auction.sellerUUID), auction.price);

            // Give Item
            buyer.getInventory().addItem(auction.item);
            
            // Notify
            buyer.sendMessage(color("&aPurchased item for $" + auction.price));
            
            // Notify Seller (if online)
            Player seller = Bukkit.getPlayer(auction.sellerUUID);
            if (seller != null) {
                seller.sendMessage(color("&aYour item was sold for $" + auction.price));
                plugin.getScoreboardManager().updateScoreboard(seller);
            }

            // Remove from AH
            auctions.remove(slot);
            saveAuctions();
            
            // Refresh GUI for buyer
            plugin.getScoreboardManager().updateScoreboard(buyer);
            openAuctionHouse(buyer);

        } else {
            buyer.sendMessage(color("&cInsufficient Funds!"));
        }
    }

    // --- DATA SAVING ---
    private void loadAuctions() {
        auctionFile = new File(plugin.getDataFolder(), "auctions.yml");
        if (!auctionFile.exists()) {
            try { auctionFile.createNewFile(); } catch (Exception e) { e.printStackTrace(); }
        }
        auctionConfig = YamlConfiguration.loadConfiguration(auctionFile);
        
        auctions.clear();
        if (auctionConfig.contains("items")) {
            for (String key : auctionConfig.getConfigurationSection("items").getKeys(false)) {
                UUID uuid = UUID.fromString(auctionConfig.getString("items." + key + ".seller"));
                double price = auctionConfig.getDouble("items." + key + ".price");
                ItemStack item = auctionConfig.getItemStack("items." + key + ".item");
                long time = auctionConfig.getLong("items." + key + ".time");
                auctions.add(new AuctionItem(uuid, item, price, time));
            }
        }
    }

    public void saveAuctions() {
        auctionConfig.set("items", null); // Clear old
        for (int i = 0; i < auctions.size(); i++) {
            AuctionItem a = auctions.get(i);
            auctionConfig.set("items." + i + ".seller", a.sellerUUID.toString());
            auctionConfig.set("items." + i + ".price", a.price);
            auctionConfig.set("items." + i + ".item", a.item);
            auctionConfig.set("items." + i + ".time", a.timestamp);
        }
        try { auctionConfig.save(auctionFile); } catch (Exception e) { e.printStackTrace(); }
    }

    private Component color(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }

    // Helper Class
    public static class AuctionItem {
        UUID sellerUUID;
        ItemStack item;
        double price;
        long timestamp;

        public AuctionItem(UUID sellerUUID, ItemStack item, double price, long timestamp) {
            this.sellerUUID = sellerUUID;
            this.item = item;
            this.price = price;
            this.timestamp = timestamp;
        }
    }
}