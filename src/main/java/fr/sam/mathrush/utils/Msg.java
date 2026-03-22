package fr.sam.mathrush.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class Msg {

    private final FileConfiguration config;

    public Msg(FileConfiguration config) {
        this.config = config;
    }

    public String get(String key) {
        String prefix = color(config.getString("messages.prefix", ""));
        String msg = config.getString("messages." + key, "&c[Missing: " + key + "]");
        return prefix + color(msg);
    }

    public String raw(String key) {
        return color(config.getString("messages." + key, key));
    }

    public void send(CommandSender sender, String key) {
        sender.sendMessage(get(key));
    }

    public void send(CommandSender sender, String key, String[][] replacements) {
        String msg = get(key);
        for (String[] r : replacements) {
            msg = msg.replace(r[0], r[1]);
        }
        sender.sendMessage(msg);
    }

    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
