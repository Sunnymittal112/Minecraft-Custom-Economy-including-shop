package net.fetal.titaneconomy.managers;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.data.DataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.UUID;

public class LevelManager {

    private final TitanEconomy plugin;
    private final DataManager dataManager;

    public LevelManager(TitanEconomy plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    public int getLevel(Player player) {
        return dataManager.levelCache.getOrDefault(player.getUniqueId(), 1); // Default Level 0 nahi, 1 hona chahiye
    }

    public int getXP(Player player) {
        return dataManager.xpCache.getOrDefault(player.getUniqueId(), 0);
    }

    public void addXP(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        int currentLevel = getLevel(player);
        int maxLevel = plugin.getConfig().getInt("leveling.max-level", 10);

        if (currentLevel >= maxLevel) return; // Max level reached

        int currentXP = getXP(player);
        int newXP = currentXP + amount;
        int requiredXP = plugin.getConfig().getInt("leveling.requirements." + currentLevel, 1000);

        // Check Level Up
        if (newXP >= requiredXP) {
            newXP = newXP - requiredXP; // Bacha hua XP carry forward
            levelUp(player, currentLevel + 1);
        }

        dataManager.xpCache.put(uuid, newXP);
    }

    private void levelUp(Player player, int newLevel) {
        UUID uuid = player.getUniqueId();
        dataManager.levelCache.put(uuid, newLevel);
        
        // --- 1. SOUND (Hype create karne ke liye) ---
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        
        // --- 2. BIG TITLE ON SCREEN ---
        Component mainTitle = Component.text("LEVEL UP!", NamedTextColor.GOLD);
        Component subTitle = Component.text("You reached Level " + newLevel, NamedTextColor.GREEN);
        
        Title title = Title.title(mainTitle, subTitle, Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(1000)));
        player.showTitle(title);

        // --- 3. CHAT MESSAGE ---
        player.sendMessage(Component.text(" ", NamedTextColor.WHITE));
        player.sendMessage(Component.text(" ➤  LEVEL UP!  ", NamedTextColor.GOLD).append(Component.text(String.valueOf(newLevel), NamedTextColor.YELLOW)));
        player.sendMessage(Component.text(" ➤  Money Multiplier: ", NamedTextColor.GRAY).append(Component.text("+" + (int)(getMultiplier(player)*100 - 100) + "% Earnings", NamedTextColor.GREEN)));
        player.sendMessage(Component.text(" ", NamedTextColor.WHITE));
    }

    // Money Multiplier Logic (High Rewards)
    public double getMultiplier(Player player) {
        int level = getLevel(player);
        // Level 1 par 1.0x (No bonus)
        // Level 2 se bonus shuru
        double boostPerLevel = plugin.getConfig().getDouble("leveling.money-multiplier", 0.5);
        
        // Formula: 1 + (Level * 0.5)
        // Lvl 1: 1.5x (Immediate boost to hook player)
        // Lvl 10: 6.0x (Massive reward)
        return 1.0 + (level * boostPerLevel);
    }

    // Visual Bar [||||||....]
    public String getProgressBar(Player player) {
        int xp = getXP(player);
        int lvl = getLevel(player);
        int maxLvl = plugin.getConfig().getInt("leveling.max-level", 10);
        
        if (lvl >= maxLvl) {
            return "&a[MAX LEVEL]";
        }

        int req = plugin.getConfig().getInt("leveling.requirements." + lvl, 1000);
        
        int totalBars = 10;
        float percent = (float) xp / req;
        int filledBars = (int) (totalBars * percent);

        StringBuilder bar = new StringBuilder("&8[");
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) bar.append("&a|"); // Green
            else bar.append("&7|"); // Grey
        }
        bar.append("&8]");
        return bar.toString();
    }
}