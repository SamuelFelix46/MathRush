package fr.sam.mathrush.listeners;

import fr.sam.mathrush.MathRush;
import fr.sam.mathrush.utils.Msg;
import fr.sam.mathrush.utils.SoundUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {

    private final MathRush plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Msg msg;
    private final SoundUtil sound;

    public ChatListener(MathRush plugin) {
        this.plugin = plugin;
        this.msg = new Msg(plugin.getConfig());
        this.sound = new SoundUtil(plugin.getConfig());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getEventManager().isActive()) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("mathrush.play")) return;

        String message = event.getMessage().trim();

        // Vérifier si c'est un nombre
        try {
            Integer.parseInt(message);
        } catch (NumberFormatException e) {
            return;
        }

        // Anti-spam
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        int cd = plugin.getConfig().getInt("answer-cooldown", 2) * 1000;
        if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < cd) return;
        cooldowns.put(uuid, now);

        // Record attempt
        plugin.getStatsManager().get(uuid).recordAttempt();

        // Check on main thread
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
            boolean correct = plugin.getEventManager().checkAnswer(player, message);
            if (!correct) {
                player.sendMessage(msg.get("wrong-answer"));
                sound.play(player, "wrong");
            }
        });
    }
}
