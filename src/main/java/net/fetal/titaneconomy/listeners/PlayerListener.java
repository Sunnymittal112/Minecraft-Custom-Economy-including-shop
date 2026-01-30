package net.fetal.titaneconomy.listeners;

import net.fetal.titaneconomy.TitanEconomy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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

        // 2. Scoreboard Setup (HUD dikhao)
        plugin.getScoreboardManager().setupScoreboard(event.getPlayer());
    }
}