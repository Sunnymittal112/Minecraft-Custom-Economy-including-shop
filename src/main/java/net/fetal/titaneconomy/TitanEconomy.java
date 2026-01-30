package net.fetal.titaneconomy;

import net.fetal.titaneconomy.api.TitanVaultHook;
import net.fetal.titaneconomy.commands.*;
import net.fetal.titaneconomy.listeners.*;
import net.fetal.titaneconomy.managers.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public class TitanEconomy extends JavaPlugin {

    private static TitanEconomy instance;
    private EconomyManager economyManager;
    private LevelManager levelManager;
    private ScoreboardManager scoreboardManager;
    private ShopManager shopManager;
    private AuctionManager auctionManager; // NEW
    private static Logger log;

    @Override
    public void onEnable() {
        instance = this;
        log = getLogger();

        log.info("-------------------------------------------");
        log.info("      TitanEconomy FINAL is Loading...     ");
        log.info("-------------------------------------------");

        saveDefaultConfig();

        // Initialize Managers
        this.economyManager = new EconomyManager(this);
        this.levelManager = new LevelManager(this, economyManager.getDataManager());
        this.scoreboardManager = new ScoreboardManager(this);
        this.shopManager = new ShopManager(this);
        this.auctionManager = new AuctionManager(this); // NEW

        // Vault Hook
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            getServer().getServicesManager().register(Economy.class, new TitanVaultHook(this), this, ServicePriority.Highest);
        }

        // Register Listeners
        getServer().getPluginManager().registerEvents(new EconomyListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new NoteListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new AuctionListener(this), this); // NEW

        // Register Commands
        if (getCommand("balance") != null) getCommand("balance").setExecutor(new BalanceCommand(this));
        if (getCommand("shop") != null) getCommand("shop").setExecutor(new ShopCommand(this));
        if (getCommand("pay") != null) getCommand("pay").setExecutor(new PayCommand(this));
        if (getCommand("eco") != null) getCommand("eco").setExecutor(new EcoCommand(this));
        if (getCommand("withdraw") != null) getCommand("withdraw").setExecutor(new WithdrawCommand(this));
        if (getCommand("baltop") != null) getCommand("baltop").setExecutor(new TopCommand(this));
        if (getCommand("ah") != null) getCommand("ah").setExecutor(new AuctionCommand(this)); // NEW

        log.info("System: ALL SYSTEMS OPERATIONAL.");
        log.info("TitanEconomy is now ENABLED!");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) economyManager.saveAllData();
        if (auctionManager != null) auctionManager.saveAuctions(); // Save Listings
        log.info("TitanEconomy DISABLED.");
    }

    // Getters
    public static TitanEconomy getInstance() { return instance; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public LevelManager getLevelManager() { return levelManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public ShopManager getShopManager() { return shopManager; }
    public AuctionManager getAuctionManager() { return auctionManager; } // NEW
}