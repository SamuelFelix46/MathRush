package fr.sam.mathrush.listeners;

import fr.sam.mathrush.MathRush;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Ajoute le joueur à la BossBar active s'il rejoint pendant un événement.
 */
public class PlayerJoinListener implements Listener {

    private final MathRush plugin;

    public PlayerJoinListener(MathRush plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Pré-charger les stats du joueur
        plugin.getStatsManager().get(event.getPlayer().getUniqueId());
    }
}
