package fr.sam.mathrush.managers;

import fr.sam.mathrush.MathRush;
import fr.sam.mathrush.models.MathQuestion;
import fr.sam.mathrush.models.PlayerData;
import fr.sam.mathrush.utils.MathGenerator;
import fr.sam.mathrush.utils.Msg;
import fr.sam.mathrush.utils.SoundUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class EventManager {

    private final MathRush plugin;
    private final MathGenerator mathGen;
    private final Msg msg;
    private final SoundUtil sound;

    private MathQuestion currentQuestion;
    private boolean active = false;
    private long startTimeMs;
    private BukkitTask timeoutTask;
    private BukkitTask displayTask;
    private BossBar bossBar;

    public EventManager(MathRush plugin) {
        this.plugin = plugin;
        this.mathGen = new MathGenerator(plugin.getConfig());
        this.msg = new Msg(plugin.getConfig());
        this.sound = new SoundUtil(plugin.getConfig());
    }

    public void startEvent() {
        if (active) return;
        if (Bukkit.getOnlinePlayers().isEmpty()) return;

        int countdown = plugin.getConfig().getInt("countdown", 5);
        if (countdown > 0) {
            runCountdown(countdown);
        } else {
            launchQuestion();
        }
    }

    private void runCountdown(int seconds) {
        active = true; // Bloquer d'autres events pendant le countdown
        new BukkitRunnable() {
            int remaining = seconds;
            @Override
            public void run() {
                if (remaining <= 0) {
                    cancel();
                    launchQuestion();
                    return;
                }
                String text = msg.raw("countdown").replace("%seconds%", String.valueOf(remaining));
                float pitch = 0.5f + (1.5f * (seconds - remaining) / (float) seconds);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(text, "", 0, 25, 5);
                }
                sound.playAll("countdown-tick", pitch);
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void launchQuestion() {
        // Réinitialiser les compteurs de réponses par joueur
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getStatsManager().get(p.getUniqueId()).resetEventAnswerCount();
        }

        // Difficulté adaptative : on utilise la difficulté médiane par défaut
        String difficulty = "medium";
        if (plugin.getConfig().getBoolean("adaptive-difficulty", true)) {
            // Prendre le streak moyen des joueurs en ligne pour ajuster
            int onlineAvgStreak = 0;
            int count = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData data = plugin.getStatsManager().get(p.getUniqueId());
                onlineAvgStreak += data.getCurrentStreak();
                count++;
            }
            if (count > 0) {
                int avg = onlineAvgStreak / count;
                if (avg >= 5) difficulty = "hard";
                else if (avg <= 1) difficulty = "easy";
            }
        }

        currentQuestion = mathGen.generate(difficulty);
        startTimeMs = System.currentTimeMillis();
        active = true;

        String calcul = currentQuestion.getDisplay();
        String title = msg.raw("event-title").replace("%calcul%", calcul);
        String subtitle = msg.raw("event-subtitle");

        // Broadcast
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(msg.get("event-start"));
            p.sendTitle(title, subtitle, 10, 70, 20);
        }
        sound.playAll("event-start");

        // Display mode
        String mode = plugin.getConfig().getString("display.mode", "BOTH").toUpperCase();

        if (mode.equals("BOSSBAR") || mode.equals("BOTH")) {
            createBossBar(calcul);
        }

        // ActionBar loop
        if (mode.equals("TITLE") || mode.equals("BOTH")) {
            String abText = Msg.color("&e⚡ MathRush &7» &f" + calcul);
            displayTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!active) { cancel(); return; }
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacy(abText));
                    }
                }
            }.runTaskTimer(plugin, 0L, 30L);
        }

        // Timeout
        int duration = plugin.getConfig().getInt("event-duration", 120);
        timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) return;
                timeout();
            }
        }.runTaskLater(plugin, duration * 20L);

        // BossBar countdown animation
        if (bossBar != null) {
            new BukkitRunnable() {
                final int totalTicks = duration * 20;
                int elapsed = 0;
                @Override
                public void run() {
                    if (!active || bossBar == null) { cancel(); return; }
                    elapsed++;
                    double progress = 1.0 - ((double) elapsed / totalTicks);
                    bossBar.setProgress(Math.max(0, Math.min(1, progress)));
                }
            }.runTaskTimer(plugin, 1L, 1L);
        }

        plugin.getLogger().info("[MathRush] Event: " + calcul + " = " + currentQuestion.getAnswer()
                + " (" + difficulty + ")");
    }

    private void createBossBar(String calcul) {
        String colorStr = plugin.getConfig().getString("display.bossbar-color", "GREEN");
        String styleStr = plugin.getConfig().getString("display.bossbar-style", "SEGMENTED_10");
        BarColor color;
        BarStyle style;
        try { color = BarColor.valueOf(colorStr); } catch (Exception e) { color = BarColor.GREEN; }
        try { style = BarStyle.valueOf(styleStr); } catch (Exception e) { style = BarStyle.SEGMENTED_10; }

        bossBar = Bukkit.createBossBar(Msg.color("&e⚡ &f" + calcul), color, style);
        bossBar.setProgress(1.0);
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(p);
        }
    }

    public boolean isActive() { return active; }

    public boolean checkAnswer(Player player, String message) {
        if (!active || currentQuestion == null) return false;

        try {
            int ans = Integer.parseInt(message.trim());
            if (ans == currentQuestion.getAnswer()) {
                win(player);
                return true;
            }
        } catch (NumberFormatException ignored) {}
        return false;
    }

    private void win(Player winner) {
        long elapsed = System.currentTimeMillis() - startTimeMs;
        double seconds = elapsed / 1000.0;

        // Stats
        PlayerData data = plugin.getStatsManager().get(winner.getUniqueId());
        data.recordWin(elapsed);

        // Broadcast
        String message = msg.get("correct-answer")
                .replace("%player%", winner.getName())
                .replace("%answer%", String.valueOf(currentQuestion.getAnswer()))
                .replace("%time%", String.format("%.1f", seconds));
        Bukkit.broadcastMessage(message);

        sound.playAll("correct");

        // Streak announce
        if (plugin.getConfig().getBoolean("streak.enabled", true)
                && plugin.getConfig().getBoolean("streak.announce", true)) {
            int threshold = plugin.getConfig().getInt("streak.threshold", 3);
            if (data.getCurrentStreak() > 0 && data.getCurrentStreak() % threshold == 0) {
                double mult = plugin.getConfig().getDouble("streak.multiplier", 2.0);
                String streakMsg = msg.get("streak-msg")
                        .replace("%player%", winner.getName())
                        .replace("%streak%", String.valueOf(data.getCurrentStreak()))
                        .replace("%multiplier%", String.format("%.1f", mult));
                Bukkit.broadcastMessage(streakMsg);
                sound.playAll("streak");
            }
        }

        // Reset other players' streaks
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(winner)) {
                plugin.getStatsManager().get(p.getUniqueId()).resetStreak();
            }
        }

        // Rewards
        plugin.getRewardManager().giveRewards(winner);

        // Save
        plugin.getStatsManager().save();

        end();
        plugin.getLogger().info("[MathRush] " + winner.getName() + " won in " + String.format("%.1f", seconds) + "s");
    }

    private void timeout() {
        String message = msg.get("event-timeout")
                .replace("%answer%", String.valueOf(currentQuestion.getAnswer()));
        Bukkit.broadcastMessage(message);
        sound.playAll("timeout");

        // Reset all streaks
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getStatsManager().get(p.getUniqueId()).resetStreak();
        }

        end();
        plugin.getLogger().info("[MathRush] Event timed out. Answer was: " + currentQuestion.getAnswer());
    }

    private void end() {
        active = false;
        currentQuestion = null;
        if (timeoutTask != null) { timeoutTask.cancel(); timeoutTask = null; }
        if (displayTask != null) { displayTask.cancel(); displayTask = null; }
        if (bossBar != null) { bossBar.removeAll(); bossBar = null; }
    }

    public void forceStop() {
        if (active) end();
    }
}
