package fr.sam.mathrush.managers;

import fr.sam.mathrush.MathRush;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SchedulerManager {

    private final MathRush plugin;
    private final List<BukkitTask> scheduled = new ArrayList<>();
    private BukkitTask dailyTask;

    public SchedulerManager(MathRush plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        scheduleToday();

        // Re-schedule every 24h
        dailyTask = new BukkitRunnable() {
            @Override
            public void run() {
                cancelEvents();
                scheduleToday();
                plugin.getLogger().info("[MathRush] Events re-scheduled for the day.");
            }
        }.runTaskTimer(plugin, 1728000L, 1728000L);
    }

    private void scheduleToday() {
        int perDay = plugin.getConfig().getInt("events-per-day", 12);
        String startStr = plugin.getConfig().getString("time.start", "08:00");
        String endStr = plugin.getConfig().getString("time.end", "22:00");
        int minGap = plugin.getConfig().getInt("min-event-gap", 3) * 60; // en secondes

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startTime = LocalTime.parse(startStr, fmt);
        LocalTime endTime = LocalTime.parse(endStr, fmt);
        LocalTime now = LocalTime.now();

        long startSec = startTime.toSecondOfDay();
        long endSec = endTime.toSecondOfDay();
        long nowSec = now.toSecondOfDay();

        if (endSec <= startSec) endSec += 86400;

        long effectiveStart = Math.max(startSec, nowSec);
        long window = endSec - effectiveStart;
        if (window <= 0) {
            plugin.getLogger().info("[MathRush] Time window closed for today.");
            return;
        }

        // Générer des timestamps aléatoires et les espacer
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < perDay * 3; i++) { // Sur-échantillonner pour avoir assez après filtrage
            times.add(effectiveStart + ThreadLocalRandom.current().nextLong(window));
        }
        times.sort(Long::compareTo);

        List<Long> finalTimes = new ArrayList<>();
        long lastTime = -minGap;
        for (long t : times) {
            if (t - lastTime >= minGap && finalTimes.size() < perDay) {
                finalTimes.add(t);
                lastTime = t;
            }
        }

        for (long t : finalTimes) {
            long delay = t - nowSec;
            if (delay <= 0) continue;

            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getEventManager().startEvent();
                }
            }.runTaskLater(plugin, delay * 20L);
            scheduled.add(task);
        }

        plugin.getLogger().info("[MathRush] " + finalTimes.size() + " events scheduled today"
                + " (" + startStr + " - " + endStr + ", gap " + (minGap/60) + "min).");
    }

    private void cancelEvents() {
        scheduled.forEach(t -> { if (t != null) t.cancel(); });
        scheduled.clear();
    }

    public void stop() {
        cancelEvents();
        if (dailyTask != null) { dailyTask.cancel(); dailyTask = null; }
    }

    public void reschedule() {
        stop();
        start();
    }
}
