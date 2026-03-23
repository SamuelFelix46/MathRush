package fr.sam.mathrush.models;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private int totalWins;
    private int totalAttempts;
    private int currentStreak;
    private int bestStreak;
    private long bestTimeMs;
    private double totalTimeMs;
    private int eventAnswerCount; // Nombre de réponses dans l'événement actuel

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.totalWins = 0;
        this.totalAttempts = 0;
        this.currentStreak = 0;
        this.bestStreak = 0;
        this.bestTimeMs = Long.MAX_VALUE;
        this.totalTimeMs = 0;
        this.eventAnswerCount = 0;
    }

    public UUID getUuid() { return uuid; }
    public int getTotalWins() { return totalWins; }
    public int getTotalAttempts() { return totalAttempts; }
    public int getCurrentStreak() { return currentStreak; }
    public int getBestStreak() { return bestStreak; }
    public long getBestTimeMs() { return bestTimeMs; }

    public double getAverageTimeMs() {
        return totalWins > 0 ? totalTimeMs / totalWins : 0;
    }

    public void recordWin(long timeMs) {
        totalWins++;
        currentStreak++;
        totalTimeMs += timeMs;
        if (currentStreak > bestStreak) bestStreak = currentStreak;
        if (timeMs < bestTimeMs) bestTimeMs = timeMs;
    }

    public void recordAttempt() {
        totalAttempts++;
    }

    public void resetStreak() {
        currentStreak = 0;
    }

    public int getEventAnswerCount() { return eventAnswerCount; }
    public void incrementEventAnswerCount() { eventAnswerCount++; }
    public void resetEventAnswerCount() { eventAnswerCount = 0; }

    // Pour sérialisation YAML
    public void setTotalWins(int v) { totalWins = v; }
    public void setTotalAttempts(int v) { totalAttempts = v; }
    public void setCurrentStreak(int v) { currentStreak = v; }
    public void setBestStreak(int v) { bestStreak = v; }
    public void setBestTimeMs(long v) { bestTimeMs = v; }
    public void setTotalTimeMs(double v) { totalTimeMs = v; }
    public double getTotalTimeMs() { return totalTimeMs; }
}
