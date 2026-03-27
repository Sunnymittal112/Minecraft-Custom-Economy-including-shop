package net.fetal.titaneconomy.listeners;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.managers.SellChestManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;

public class SellChestListener implements Listener {

    private final SellChestManager sellChestManager;
    private final TitanEconomy plugin;

    public SellChestListener(TitanEconomy plugin) {
        this.plugin = plugin;
        this.sellChestManager = plugin.getSellChestManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.CHEST) {
            return;
        }

        if (sellChestManager.isAdjacentToSellChest(event.getBlockPlaced())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Auto Sell Chests must stay single and cannot connect to other chests.", NamedTextColor.RED));
            return;
        }

        if (!sellChestManager.isSellChestItem(event.getItemInHand())) {
            return;
        }

        if (sellChestManager.isAdjacentToChest(event.getBlockPlaced())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Auto Sell Chests must stay single and cannot connect to other chests.", NamedTextColor.RED));
            return;
        }

        sellChestManager.registerSellChest(event.getPlayer(), event.getBlockPlaced());
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.2f);
        event.getPlayer().sendMessage(Component.text("Placed an Auto Sell Chest. It will auto-sell every " + sellChestManager.getIntervalDescription() + " while this chunk is loaded.", NamedTextColor.GREEN));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!sellChestManager.isSellChestBlock(event.getBlock())) {
            return;
        }

        Chest chest = (Chest) event.getBlock().getState();
        ItemStack[] contents = chest.getBlockInventory().getContents();
        chest.getBlockInventory().clear();

        sellChestManager.unregisterSellChest(event.getBlock());
        event.setDropItems(false);

        boolean dropsAtFeet = plugin.getConfig().getBoolean("settings.break-drops-at-feet", true);
        Location dropLocation = dropsAtFeet
            ? event.getPlayer().getLocation().add(0.0D, 0.2D, 0.0D)
            : event.getBlock().getLocation();

        for (ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            if (dropsAtFeet) {
                Item droppedItem = event.getBlock().getWorld().dropItem(dropLocation, item);
                droppedItem.setVelocity(new Vector(0.0D, 0.0D, 0.0D));
            } else {
                event.getBlock().getWorld().dropItemNaturally(dropLocation, item);
            }
        }

        if (dropsAtFeet) {
            Item droppedChest = event.getBlock().getWorld().dropItem(dropLocation, sellChestManager.createDroppedSellChest());
            droppedChest.setVelocity(new Vector(0.0D, 0.0D, 0.0D));
        } else {
            event.getBlock().getWorld().dropItemNaturally(dropLocation, sellChestManager.createDroppedSellChest());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        unregisterExplodedSellChests(event.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        unregisterExplodedSellChests(event.blockList());
    }

    private void unregisterExplodedSellChests(List<Block> blocks) {
        for (Block block : blocks) {
            if (sellChestManager.isSellChestBlock(block)) {
                sellChestManager.unregisterSellChest(block);
            }
        }
    }
}
