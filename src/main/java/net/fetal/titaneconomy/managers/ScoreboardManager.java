package net.fetal.titaneconomy.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import net.fetal.titaneconomy.TitanEconomy;
import net.kyori.adventure.text.Component;

public class ScoreboardManager {

    private final TitanEconomy plugin;

    public ScoreboardManager(TitanEconomy plugin) {
        this.plugin = plugin;
    }

    public void setupScoreboard(Player player) {
        // Naya Scoreboard create karo
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        
        // Sidebar Objective banao
        // Title: Gold & Bold "TITAN NETWORK"
        Objective obj = board.registerNewObjective("TitanHUD", Criteria.DUMMY, Component.text("  §6§lTITAN NETWORK  "));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Scoreboard player ko assign karo
        player.setScoreboard(board);
        
        // First update trigger karo taaki text dikhe
        updateScoreboard(player);
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective("TitanHUD");

        if (obj == null) return;

        // Data fetch karo
        double bal = plugin.getEconomyManager().getBalance(player);
        int level = plugin.getLevelManager().getLevel(player);
        String currency = plugin.getConfig().getString("settings.currency-symbol", "$");

        // --- LINES SETUP ---
        // 15 = Top, 1 = Bottom.
        
        setLine(board, obj, "§7----------------", 15);
        setLine(board, obj, "§fUser:", 14);
        setLine(board, obj, "§e" + player.getName(), 13);
        setLine(board, obj, "§1 ", 12); // Empty Line (Invisible)
        setLine(board, obj, "§fBalance:", 11);
        setLine(board, obj, "§a" + currency + String.format("%.1f", bal), 10);
        setLine(board, obj, "§2 ", 9); // Empty Line
        setLine(board, obj, "§fLevel:", 8);
        setLine(board, obj, "§b" + level, 7);
        setLine(board, obj, "§3 ", 6); // Empty Line
        setLine(board, obj, "§7----------------", 5);
        setLine(board, obj, "§6play.titan.com", 4);
    }

    // Helper method to reduce flicker
    private void setLine(Scoreboard board, Objective obj, String text, int score) {
        // Purana entry dhoondo jo same score par hai
        for (String entry : board.getEntries()) {
            if (obj.getScore(entry).getScore() == score) {
                // Agar text change hua hai, tabhi replace karo
                if (!entry.equals(text)) {
                    board.resetScores(entry); // Delete old
                }
            }
        }
        // Set new
        Score scoreObj = obj.getScore(text);
        scoreObj.setScore(score);
    }
}