package net.fetal.titaneconomy.listeners;

import net.fetal.titaneconomy.TitanEconomy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.GameMode;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;

public class PlayerListener implements Listener {

    private final TitanEconomy plugin;

    public PlayerListener(TitanEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // 1. First time account check
        if (!plugin.getEconomyManager().hasAccount(event.getPlayer())) {
            plugin.getEconomyManager().createAccount(event.getPlayer());
        }

        // 2. Set up the scoreboard HUD
        plugin.getScoreboardManager().setupScoreboard(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnerBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("settings.spawners-drop-themselves", true)) {
            return;
        }

        if (event.getBlock().getType() != Material.SPAWNER) {
            return;
        }

        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (!(event.getBlock().getState() instanceof CreatureSpawner spawner)) {
            return;
        }

        EntityType spawnerType = spawner.getSpawnedType();
        ItemStack droppedSpawner = plugin.getShopManager().createSpawnerItem(spawnerType, 1);
        boolean dropsAtFeet = plugin.getConfig().getBoolean("settings.break-drops-at-feet", true);

        event.setDropItems(false);
        event.setExpToDrop(0);

        if (dropsAtFeet) {
            Location dropLocation = event.getPlayer().getLocation().add(0.0D, 0.2D, 0.0D);
            Item drop = event.getBlock().getWorld().dropItem(dropLocation, droppedSpawner);
            drop.setVelocity(new Vector(0.0D, 0.0D, 0.0D));
            return;
        }

        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), droppedSpawner);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (!plugin.getConfig().getBoolean("settings.break-drops-at-feet", true)) {
            return;
        }

        Location dropLocation = event.getPlayer().getLocation().add(0.0D, 0.2D, 0.0D);
        List<Item> drops = event.getItems();
        for (Item drop : drops) {
            drop.teleport(dropLocation);
            drop.setVelocity(new Vector(0.0D, 0.0D, 0.0D));
        }
    }
}
