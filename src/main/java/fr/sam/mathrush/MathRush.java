package fr.sam.mathrush;

import fr.sam.mathrush.commands.MathRushCommand;
import fr.sam.mathrush.commands.MathRushTabCompleter;
import fr.sam.mathrush.listeners.ChatListener;
import fr.sam.mathrush.listeners.PlayerJoinListener;
import fr.sam.mathrush.managers.EventManager;
import fr.sam.mathrush.managers.RewardManager;
import fr.sam.mathrush.managers.SchedulerManager;
import fr.sam.mathrush.managers.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MathRush extends JavaPlugin {

    private static MathRush instance;
    private EventManager eventManager;
    private RewardManager rewardManager;
    private SchedulerManager schedulerManager;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        statsManager = new StatsManager(this);
        rewardManager = new RewardManager(this);
        eventManager = new EventManager(this);
        schedulerManager = new SchedulerManager(this);

        // Commands
        getCommand("mathrush").setExecutor(new MathRushCommand(this));
        getCommand("mathrush").setTabCompleter(new MathRushTabCompleter());

        // Listeners
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Start scheduler
        schedulerManager.start();

        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        getLogger().info("  MathRush v" + getDescription().getVersion());
        getLogger().info("  Events/day: " + getConfig().getInt("events-per-day"));
        getLogger().info("  Time: " + getConfig().getString("time.start") + " - " + getConfig().getString("time.end"));
        getLogger().info("  Rewards: " + getConfig().getStringList("rewards").size() + " configured");
        getLogger().info("  Players tracked: " + statsManager.getTopPlayers(999).size());
        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Override
    public void onDisable() {
        if (eventManager != null) eventManager.forceStop();
        if (schedulerManager != null) schedulerManager.stop();
        if (statsManager != null) statsManager.save();
        getLogger().info("MathRush disabled. Data saved.");
    }

    public static MathRush getInstance() { return instance; }
    public EventManager getEventManager() { return eventManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public SchedulerManager getSchedulerManager() { return schedulerManager; }
    public StatsManager getStatsManager() { return statsManager; }
}
