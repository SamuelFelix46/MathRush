package fr.sam.mathrush.commands;

import fr.sam.mathrush.MathRush;
import fr.sam.mathrush.models.PlayerData;
import fr.sam.mathrush.utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MathRushCommand implements CommandExecutor {

    private final MathRush plugin;
    private final Msg msg;

    public MathRushCommand(MathRush plugin) {
        this.plugin = plugin;
        this.msg = new Msg(plugin.getConfig());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(msg.get("usage"));
            return true;
        }

        String sub = args[0].toLowerCase();

        // Stats et top : permission mathrush.stats
        if (sub.equals("stats") || sub.equals("top")) {
            if (!sender.hasPermission("mathrush.stats")) {
                sender.sendMessage(msg.get("no-permission"));
                return true;
            }
            if (sub.equals("stats")) return handleStats(sender, args);
            return handleTop(sender);
        }

        // Tout le reste : admin
        if (!sender.hasPermission("mathrush.admin")) {
            sender.sendMessage(msg.get("no-permission"));
            return true;
        }

        switch (sub) {
            case "defnum" -> handleDefNum(sender, args);
            case "run" -> handleRun(sender);
            case "defreward" -> handleDefReward(sender, args);
            case "supprreward" -> handleSupprReward(sender);
            case "time" -> handleTime(sender, args);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage(msg.get("usage"));
        }
        return true;
    }

    private void handleDefNum(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Msg.color("&cUsage : /mr defnum <1-600>"));
            return;
        }
        try {
            int n = Integer.parseInt(args[1]);
            if (n < 1 || n > 600) {
                sender.sendMessage(Msg.color("&cValeur entre 1 et 600."));
                return;
            }
            plugin.getConfig().set("events-per-day", n);
            plugin.saveConfig();
            plugin.getSchedulerManager().reschedule();
            sender.sendMessage(msg.get("defnum-set").replace("%count%", String.valueOf(n)));
        } catch (NumberFormatException e) {
            sender.sendMessage(Msg.color("&cNombre invalide."));
        }
    }

    private void handleRun(CommandSender sender) {
        plugin.getEventManager().startEvent();
        sender.sendMessage(msg.get("event-forced"));
    }

    private void handleDefReward(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Msg.color("&cUsage : /mr defreward <ITEM:qté> ..."));
            return;
        }
        List<String> rewards = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            if (!args[i].contains(":")) {
                sender.sendMessage(Msg.color("&cFormat invalide : " + args[i]));
                return;
            }
            rewards.add(args[i].toUpperCase());
        }
        plugin.getRewardManager().setRewards(rewards);
        sender.sendMessage(msg.get("rewards-added"));
    }

    private void handleSupprReward(CommandSender sender) {
        plugin.getRewardManager().clearRewards();
        sender.sendMessage(msg.get("rewards-cleared"));
    }

    private void handleTime(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Msg.color("&cUsage : /mr time <HH:mm> <HH:mm>"));
            return;
        }
        if (!args[1].matches("\\d{2}:\\d{2}") || !args[2].matches("\\d{2}:\\d{2}")) {
            sender.sendMessage(Msg.color("&cFormat HH:mm requis."));
            return;
        }
        plugin.getConfig().set("time.start", args[1]);
        plugin.getConfig().set("time.end", args[2]);
        plugin.saveConfig();
        plugin.getSchedulerManager().reschedule();
        sender.sendMessage(msg.get("time-set")
                .replace("%start%", args[1]).replace("%end%", args[2]));
    }

    private boolean handleStats(CommandSender sender, String[] args) {
        UUID target;
        String name;

        if (args.length >= 2) {
            Player p = Bukkit.getPlayer(args[1]);
            if (p == null) {
                sender.sendMessage(Msg.color("&cJoueur introuvable."));
                return true;
            }
            target = p.getUniqueId();
            name = p.getName();
        } else if (sender instanceof Player) {
            target = ((Player) sender).getUniqueId();
            name = sender.getName();
        } else {
            sender.sendMessage(Msg.color("&cSpécifiez un joueur."));
            return true;
        }

        PlayerData d = plugin.getStatsManager().get(target);
        sender.sendMessage(msg.get("stats-header").replace("%player%", name));
        sendStat(sender, "Victoires", String.valueOf(d.getTotalWins()));
        sendStat(sender, "Tentatives", String.valueOf(d.getTotalAttempts()));
        sendStat(sender, "Série actuelle", String.valueOf(d.getCurrentStreak()));
        sendStat(sender, "Meilleure série", String.valueOf(d.getBestStreak()));
        String bestTime = d.getBestTimeMs() == Long.MAX_VALUE ? "-" :
                String.format("%.1fs", d.getBestTimeMs() / 1000.0);
        sendStat(sender, "Meilleur temps", bestTime);
        String avgTime = d.getTotalWins() == 0 ? "-" :
                String.format("%.1fs", d.getAverageTimeMs() / 1000.0);
        sendStat(sender, "Temps moyen", avgTime);
        return true;
    }

    private void sendStat(CommandSender sender, String label, String value) {
        sender.sendMessage(msg.raw("stats-line")
                .replace("%label%", label).replace("%value%", value));
    }

    private boolean handleTop(CommandSender sender) {
        var top = plugin.getStatsManager().getTopPlayers(10);
        sender.sendMessage(msg.get("top-header"));
        if (top.isEmpty()) {
            sender.sendMessage(msg.raw("top-empty"));
            return true;
        }
        int rank = 1;
        for (Map.Entry<UUID, PlayerData> entry : top) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(entry.getKey());
            String pName = op.getName() != null ? op.getName() : entry.getKey().toString().substring(0, 8);
            sender.sendMessage(msg.raw("top-entry")
                    .replace("%rank%", String.valueOf(rank++))
                    .replace("%player%", pName)
                    .replace("%wins%", String.valueOf(entry.getValue().getTotalWins()))
                    .replace("%streak%", String.valueOf(entry.getValue().getBestStreak())));
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.getSchedulerManager().reschedule();
        sender.sendMessage(msg.get("reloaded"));
    }
}
