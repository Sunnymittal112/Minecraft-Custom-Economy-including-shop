package net.fetal.titaneconomy.api;

import net.fetal.titaneconomy.TitanEconomy;
import net.fetal.titaneconomy.managers.EconomyManager;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class TitanVaultHook extends AbstractEconomy {

    private final TitanEconomy plugin;
    private final EconomyManager economyManager;

    public TitanVaultHook(TitanEconomy plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "TitanEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return "$" + String.format("%.2f", amount);
    }

    @Override
    public String currencyNamePlural() {
        return "Dollars";
    }

    @Override
    public String currencyNameSingular() {
        return "Dollar";
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return economyManager.hasAccount(player);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return economyManager.getBalance(player);
    }

    // --- WITHDRAW LOGIC ---
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount");
        
        if (!hasAccount(player)) economyManager.createAccount(player);

        if (economyManager.withdraw(player, amount)) {
            return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
        } else {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
    }

    // --- DEPOSIT LOGIC ---
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount");

        if (!hasAccount(player)) economyManager.createAccount(player);

        economyManager.deposit(player, amount);
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
    }

    // --- REQUIRED STRING METHODS (Legacy Support) ---
    // Vault requires these even if we prefer OfflinePlayer
    
    @Override
    public boolean hasAccount(String playerName) {
        return hasAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public double getBalance(String playerName) {
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        economyManager.createAccount(Bukkit.getOfflinePlayer(playerName));
        return true;
    }

    // --- THE FIX: MISSING ABSTRACT METHOD ---
    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        // Hum World Name ignore karenge, kyunki Economy Global hai
        return createPlayerAccount(playerName);
    }
    
    // --- Other World-Specific Methods (Redirect to Global) ---
    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    // --- UNUSED BANK METHODS ---
    @Override public boolean has(String playerName, double amount) { return getBalance(playerName) >= amount; }
    @Override public boolean has(OfflinePlayer player, double amount) { return getBalance(player) >= amount; }
    @Override public EconomyResponse createBank(String name, String player) { return null; }
    @Override public EconomyResponse createBank(String name, OfflinePlayer player) { return null; }
    @Override public EconomyResponse deleteBank(String name) { return null; }
    @Override public EconomyResponse bankBalance(String name) { return null; }
    @Override public EconomyResponse bankHas(String name, double amount) { return null; }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return null; }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return null; }
    @Override public EconomyResponse isBankOwner(String name, String playerName) { return null; }
    @Override public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return null; }
    @Override public EconomyResponse isBankMember(String name, String playerName) { return null; }
    @Override public EconomyResponse isBankMember(String name, OfflinePlayer player) { return null; }
    @Override public List<String> getBanks() { return null; }
    @Override public boolean createPlayerAccount(OfflinePlayer player) { economyManager.createAccount(player); return true; }
    @Override public boolean createPlayerAccount(OfflinePlayer player, String worldName) { return createPlayerAccount(player); }
}