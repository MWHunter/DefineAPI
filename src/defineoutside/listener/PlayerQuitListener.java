package defineoutside.listener;

import defineoutside.creator.DefinePlayer;
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
        DefinePlayer definePlayer = PlayerManager.getDefinePlayer(event.getPlayer().getUniqueId());
        for (Entry<UUID, Game> entry : GameManager.getGamesHashMap().entrySet()) {
            Game playerGame = entry.getValue();

            if (playerGame.getUuidParticipating().contains(definePlayer)) {
                playerGame.playerLeave(definePlayer);
                // We found it, now stop concurrent modification exception
                break;
            }
        }

        // Stop the player from matchmaking
        Matchmaking mm = new Matchmaking();
        mm.removePlayer(definePlayer);

        definePlayer.removePlayer();

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
