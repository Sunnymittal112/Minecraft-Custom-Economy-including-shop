package net.fetal.titaneconomy.managers;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import net.fetal.titaneconomy.TitanEconomy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ScoreboardManager {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private final TitanEconomy plugin;

    public ScoreboardManager(TitanEconomy plugin) {
        this.plugin = plugin;
    }

    public void setupScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective obj = board.registerNewObjective("TitanHUD", Criteria.DUMMY, getHeaderComponent());
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.numberFormat(NumberFormat.blank());

        player.setScoreboard(board);
        updateScoreboard(player);
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective("TitanHUD");

        if (obj == null) return;

        double bal = plugin.getEconomyManager().getBalance(player);
        int level = plugin.getLevelManager().getLevel(player);
        String currency = plugin.getConfig().getString("settings.currency-symbol", "$");
        obj.displayName(getHeaderComponent());
        obj.numberFormat(NumberFormat.blank());

        setLine(obj, "&7----------------", 15);
        setLine(obj, "&fUser:", 14);
        setLine(obj, "&e" + player.getName(), 13);
        setLine(obj, " ", 12);
        setLine(obj, "&fBalance:", 11);
        setLine(obj, "&a" + currency + String.format("%.1f", bal), 10);
        setLine(obj, " ", 9);
        setLine(obj, "&fLevel:", 8);
        setLine(obj, "&b" + level, 7);
        setLine(obj, " ", 6);
        setLine(obj, "&7----------------", 5);
        setLine(obj, getFooterText(), 4);
    }

    private void setLine(Objective obj, String text, int score) {
        Score scoreObj = obj.getScore(getEntryKey(score));
        scoreObj.customName(toComponent(text));
        scoreObj.numberFormat(NumberFormat.blank());
        scoreObj.setScore(score);
    }

    private Component getHeaderComponent() {
        return toComponent(plugin.getConfig().getString("scoreboard.header", "  &6&lTITAN NETWORK  "));
    }

    private String getFooterText() {
        return plugin.getConfig().getString("scoreboard.footer", "&6play.titan.com");
    }

    private Component toComponent(String text) {
        return LEGACY_SERIALIZER.deserialize(text);
    }

    private String getEntryKey(int score) {
        return "line-" + score;
    }
}
