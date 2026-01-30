package net.fetal.titaneconomy.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.managers.EconomyManager;
import net.fetal.titaneconomy.managers.LevelManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class EconomyListener implements Listener {

    private final TitanEconomy plugin;
    private final EconomyManager economyManager;
    private final LevelManager levelManager;

    public EconomyListener(TitanEconomy plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        this.levelManager = plugin.getLevelManager();
    }

    // --- 🧟 MOB KILLING LOGIC ---
    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        
        Player player = event.getEntity().getKiller();
        String mobName = event.getEntityType().name();

        if (!plugin.getConfig().getBoolean("earnings.mobs.enabled")) return;

        double basePrice = plugin.getConfig().getDouble("earnings.mobs.values." + mobName, 0.0);
        int xpReward = plugin.getConfig().getInt("earnings.mobs.xp-reward", 10);

        if (basePrice > 0) {
            giveRewards(player, basePrice, xpReward);
        }
    }

    // --- ⛏️ MINING LOGIC ---
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode().name().equals("CREATIVE")) return;
        if (!plugin.getConfig().getBoolean("earnings.mining.enabled")) return;

        Material blockType = event.getBlock().getType();
        
        double basePrice = plugin.getConfig().getDouble("earnings.mining.values." + blockType.name(), 0.0);
        int xpReward = plugin.getConfig().getInt("earnings.mining.xp-reward", 5);

        if (basePrice > 0) {
            giveRewards(player, basePrice, xpReward);
        }
    }

    // --- HELPER METHOD ---
    private void giveRewards(Player player, double basePrice, int xp) {
        // 1. Calculate Multiplier
        double multiplier = levelManager.getMultiplier(player);
        double finalPrice = basePrice * multiplier;

        // 2. Deposit Money & Add XP
        economyManager.deposit(player, finalPrice);
        levelManager.addXP(player, xp);

        // 3. Show Action Bar
        sendActionBar(player, finalPrice, xp);
        
        // 4. UPDATE SCOREBOARD (IMPORTANT)
        // Jaise hi paisa mile, HUD update karo
        plugin.getScoreboardManager().updateScoreboard(player);
    }

    private void sendActionBar(Player player, double money, int xp) {
        String symbol = plugin.getConfig().getString("settings.currency-symbol", "$");
        String bar = levelManager.getProgressBar(player);
        int lvl = levelManager.getLevel(player);

        String msg = "&a+" + symbol + String.format("%.1f", money) + " &8| &eLvl " + lvl + " " + bar + " &b+" + xp + "XP";
        player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));
    }
}