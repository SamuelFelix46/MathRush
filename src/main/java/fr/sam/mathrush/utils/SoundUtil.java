package fr.sam.mathrush.utils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SoundUtil {

    private final FileConfiguration config;

    public SoundUtil(FileConfiguration config) {
        this.config = config;
    }

    public void play(Player player, String key) {
        String soundName = config.getString("sounds." + key, null);
        if (soundName == null) return;
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void playAll(String key) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            play(p, key);
        }
    }

    public void play(Player player, String key, float pitch) {
        String soundName = config.getString("sounds." + key, null);
        if (soundName == null) return;
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, pitch);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void playAll(String key, float pitch) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            play(p, key, pitch);
        }
    }
}
