package fr.sam.mathrush.managers;

import fr.sam.mathrush.MathRush;
import fr.sam.mathrush.models.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StatsManager {

    private final MathRush plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public StatsManager(MathRush plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        loadData();
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, PlayerData::new);
    }

    public void save() {
        for (Map.Entry<UUID, PlayerData> entry : cache.entrySet()) {
            String path = "players." + entry.getKey().toString();
            PlayerData d = entry.getValue();
            dataConfig.set(path + ".wins", d.getTotalWins());
            dataConfig.set(path + ".attempts", d.getTotalAttempts());
            dataConfig.set(path + ".streak", d.getCurrentStreak());
            dataConfig.set(path + ".best-streak", d.getBestStreak());
            dataConfig.set(path + ".best-time", d.getBestTimeMs());
            dataConfig.set(path + ".total-time", d.getTotalTimeMs());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Impossible de sauvegarder playerdata.yml : " + e.getMessage());
        }
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Impossible de créer playerdata.yml");
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.contains("players")) {
            for (String key : dataConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                String path = "players." + key;
                PlayerData d = new PlayerData(uuid);
                d.setTotalWins(dataConfig.getInt(path + ".wins", 0));
                d.setTotalAttempts(dataConfig.getInt(path + ".attempts", 0));
                d.setCurrentStreak(dataConfig.getInt(path + ".streak", 0));
                d.setBestStreak(dataConfig.getInt(path + ".best-streak", 0));
                d.setBestTimeMs(dataConfig.getLong(path + ".best-time", Long.MAX_VALUE));
                d.setTotalTimeMs(dataConfig.getDouble(path + ".total-time", 0));
                cache.put(uuid, d);
            }
        }
        plugin.getLogger().info("Chargement de " + cache.size() + " profils joueur.");
    }

    /**
     * Retourne le top N joueurs triés par victoires.
     */
    public List<Map.Entry<UUID, PlayerData>> getTopPlayers(int limit) {
        return cache.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().getTotalWins(), a.getValue().getTotalWins()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
