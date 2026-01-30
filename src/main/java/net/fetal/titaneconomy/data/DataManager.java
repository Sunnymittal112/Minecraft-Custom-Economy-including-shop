package net.fetal.titaneconomy.data;

import net.fetal.titaneconomy.TitanEconomy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class DataManager {

    private final TitanEconomy plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    // RAM Caches (Must be Public)
    public final HashMap<UUID, Double> balanceCache = new HashMap<>();
    public final HashMap<UUID, Integer> levelCache = new HashMap<>(); // NEW
    public final HashMap<UUID, Integer> xpCache = new HashMap<>();    // NEW

    public DataManager(TitanEconomy plugin) {
        this.plugin = plugin;
        createDataFile();
        loadPlayerData();
    }

    private void createDataFile() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadPlayerData() {
        if (dataConfig.contains("balances")) {
            for (String key : dataConfig.getConfigurationSection("balances").getKeys(false)) {
                balanceCache.put(UUID.fromString(key), dataConfig.getDouble("balances." + key));
            }
        }
        if (dataConfig.contains("levels")) {
            for (String key : dataConfig.getConfigurationSection("levels").getKeys(false)) {
                levelCache.put(UUID.fromString(key), dataConfig.getInt("levels." + key));
            }
        }
        if (dataConfig.contains("xp")) {
            for (String key : dataConfig.getConfigurationSection("xp").getKeys(false)) {
                xpCache.put(UUID.fromString(key), dataConfig.getInt("xp." + key));
            }
        }
        plugin.getLogger().info("Player data loaded into RAM.");
    }

    public void savePlayerData() {
        for (UUID uuid : balanceCache.keySet()) {
            dataConfig.set("balances." + uuid, balanceCache.get(uuid));
        }
        for (UUID uuid : levelCache.keySet()) {
            dataConfig.set("levels." + uuid, levelCache.get(uuid));
        }
        for (UUID uuid : xpCache.keySet()) {
            dataConfig.set("xp." + uuid, xpCache.get(uuid));
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data!");
        }
    }
}