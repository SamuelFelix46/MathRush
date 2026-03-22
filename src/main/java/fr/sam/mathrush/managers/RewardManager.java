package fr.sam.mathrush.managers;

import fr.sam.mathrush.MathRush;
import fr.sam.mathrush.models.PlayerData;
import fr.sam.mathrush.utils.Msg;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RewardManager {

    private final MathRush plugin;
    private final Msg msg;

    public RewardManager(MathRush plugin) {
        this.plugin = plugin;
        this.msg = new Msg(plugin.getConfig());
    }

    public void giveRewards(Player player) {
        List<String> rewards = plugin.getConfig().getStringList("rewards");
        if (rewards.isEmpty()) {
            player.sendMessage(msg.get("no-rewards"));
            return;
        }

        int min = plugin.getConfig().getInt("reward-count.min", 1);
        int max = plugin.getConfig().getInt("reward-count.max", 2);
        int count = ThreadLocalRandom.current().nextInt(min, Math.max(min, max) + 1);
        count = Math.min(count, rewards.size());

        // Streak multiplier
        double multiplier = 1.0;
        if (plugin.getConfig().getBoolean("streak.enabled", true)) {
            PlayerData data = plugin.getStatsManager().get(player.getUniqueId());
            int threshold = plugin.getConfig().getInt("streak.threshold", 3);
            if (data.getCurrentStreak() > 0 && data.getCurrentStreak() % threshold == 0) {
                multiplier = plugin.getConfig().getDouble("streak.multiplier", 2.0);
            }
        }

        List<String> shuffled = new ArrayList<>(rewards);
        Collections.shuffle(shuffled);

        for (int i = 0; i < count; i++) {
            giveItem(player, shuffled.get(i), multiplier);
        }
    }

    private void giveItem(Player player, String rewardStr, double multiplier) {
        String[] parts = rewardStr.split(":");
        if (parts.length != 2) return;

        Material mat = Material.matchMaterial(parts[0].trim().toUpperCase());
        if (mat == null) {
            plugin.getLogger().warning("Matériau invalide : " + parts[0]);
            return;
        }

        int amount;
        try {
            amount = (int) Math.ceil(Integer.parseInt(parts[1].trim()) * multiplier);
        } catch (NumberFormatException e) {
            return;
        }

        ItemStack item = new ItemStack(mat, Math.min(amount, 64));
        player.getInventory().addItem(item);

        String itemName = mat.name().toLowerCase().replace("_", " ");
        String message = msg.get("reward-given")
                .replace("%amount%", String.valueOf(amount))
                .replace("%item%", itemName);
        player.sendMessage(message);
    }

    public void setRewards(List<String> rewards) {
        plugin.getConfig().set("rewards", rewards);
        plugin.saveConfig();
    }

    public void clearRewards() {
        plugin.getConfig().set("rewards", new ArrayList<>());
        plugin.saveConfig();
    }
}
