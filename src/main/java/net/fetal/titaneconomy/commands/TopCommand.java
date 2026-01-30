package net.fetal.titaneconomy.commands;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.data.DataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TopCommand implements CommandExecutor {

    private final TitanEconomy plugin;

    public TopCommand(TitanEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        sender.sendMessage(Component.text("Calculating Top 10 Richest...", NamedTextColor.GRAY));

        // Async Run (Taaki server lag na kare agar 1000 players hon)
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            DataManager data = plugin.getEconomyManager().getDataManager();
            
            // Map ko sort karna (Highest Value First)
            List<Map.Entry<UUID, Double>> topList = data.balanceCache.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Sort Logic
                    .limit(10) // Sirf top 10
                    .collect(Collectors.toList());

            // Header
            sender.sendMessage(Component.text(" ", NamedTextColor.WHITE));
            sender.sendMessage(Component.text("----------[ 🏆 RICH LIST 🏆 ]----------", NamedTextColor.GOLD));

            int rank = 1;
            for (Map.Entry<UUID, Double> entry : topList) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
                String name = (p.getName() != null) ? p.getName() : "Unknown";
                String bal = String.format("%,.0f", entry.getValue());

                // Format: #1 FeTaL - $50,000
                NamedTextColor color = (rank == 1) ? NamedTextColor.YELLOW : NamedTextColor.WHITE;
                
                sender.sendMessage(Component.text("#" + rank + " ", NamedTextColor.GRAY)
                        .append(Component.text(name, color))
                        .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("$" + bal, NamedTextColor.GREEN)));
                
                rank++;
            }
            sender.sendMessage(Component.text("---------------------------------------", NamedTextColor.GOLD));
        });

        return true;
    }
}