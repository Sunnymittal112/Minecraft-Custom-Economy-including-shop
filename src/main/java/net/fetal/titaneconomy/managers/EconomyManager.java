package net.fetal.titaneconomy.managers;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.data.DataManager;
import org.bukkit.OfflinePlayer;

public class EconomyManager {

    private final TitanEconomy plugin;
    private final DataManager dataManager;
    private final double startBalance;

    public EconomyManager(TitanEconomy plugin) {
        this.plugin = plugin;
        this.dataManager = new DataManager(plugin);
        this.startBalance = plugin.getConfig().getDouble("settings.start-balance", 500.0);
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public boolean hasAccount(OfflinePlayer player) {
        return dataManager.balanceCache.containsKey(player.getUniqueId());
    }

    public void createAccount(OfflinePlayer player) {
        if (!hasAccount(player)) {
            dataManager.balanceCache.put(player.getUniqueId(), startBalance);
        }
    }

    public double getBalance(OfflinePlayer player) {
        if (!hasAccount(player)) createAccount(player);
        return dataManager.balanceCache.get(player.getUniqueId());
    }

    public void deposit(OfflinePlayer player, double amount) {
        if (!hasAccount(player)) createAccount(player);
        double current = getBalance(player);
        dataManager.balanceCache.put(player.getUniqueId(), current + amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (!hasAccount(player)) createAccount(player);
        double current = getBalance(player);
        if (current >= amount) {
            dataManager.balanceCache.put(player.getUniqueId(), current - amount);
            return true;
        }
        return false;
    }

    public void setBalance(OfflinePlayer player, double amount) {
        dataManager.balanceCache.put(player.getUniqueId(), amount);
    }

    public void saveAllData() {
        dataManager.savePlayerData();
    }
}