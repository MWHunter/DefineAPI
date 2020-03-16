package defineoutside.listener;

import defineoutside.creator.Game;
import defineoutside.main.GameManager;
import defineoutside.main.MainAPI;
import defineoutside.main.Matchmaking;
import defineoutside.main.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Map.Entry;
import java.util.UUID;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GameManager gm = new GameManager();
        for (Entry<UUID, Game> entry : gm.getGamesHashMap().entrySet()) {
            Game playerGame = entry.getValue();

            if (playerGame.getUuidParticipating().contains(event.getPlayer().getUniqueId())) {
                playerGame.playerLeave(event.getPlayer());
                // We found it, now stop concurrent modification exception
                break;
            }
        }

        // Unregister the player's object
        PlayerManager pm = new PlayerManager();
        pm.getDefinePlayer(event.getPlayer()).removePlayer();

        // Stop the player from matchmaking
        Matchmaking mm = new Matchmaking();
        // TODO: Fix this
        //mm.removePlayer(event.getPlayer().getUniqueId());

        // Delete player's data file asynchronously when they quit, to remove inventory and position data.  Space isn't a concern
        new BukkitRunnable() {
            UUID playerQuitUUID = event.getPlayer().getUniqueId();

            public void run() {
                File file = new File(new File("./world/playerdata"), playerQuitUUID + ".dat");

                file.delete();
            }
        }.runTaskLaterAsynchronously(MainAPI.getPlugin(), 1L);
    }
}
