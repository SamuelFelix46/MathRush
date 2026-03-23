package fr.sam.mathrush.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MathRushTabCompleter implements TabCompleter {

    private static final List<String> SUBS = Arrays.asList(
            "defnum", "run", "defreward", "supprreward", "time", "stats", "top", "reload"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(SUBS, args[0]);
        }

        switch (args[0].toLowerCase()) {
            case "defnum":
                if (args.length == 2) return Arrays.asList("5", "10", "20", "50");
                break;
            case "time":
                if (args.length == 2) return Arrays.asList("06:00", "08:00", "10:00");
                if (args.length == 3) return Arrays.asList("20:00", "22:00", "00:00");
                break;
            case "defreward":
                if (args.length >= 2) {
                    String partial = args[args.length - 1].toUpperCase();
                    if (partial.contains(":")) return Arrays.asList(partial.split(":")[0] + ":1",
                            partial.split(":")[0] + ":5", partial.split(":")[0] + ":10");
                    return Arrays.stream(Material.values())
                            .filter(Material::isItem)
                            .map(m -> m.name() + ":1")
                            .filter(s -> s.startsWith(partial))
                            .limit(20)
                            .collect(Collectors.toList());
                }
                break;
            case "stats":
                if (args.length == 2) {
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
                break;
        }
        return new ArrayList<>();
    }

    private List<String> filter(List<String> list, String prefix) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
