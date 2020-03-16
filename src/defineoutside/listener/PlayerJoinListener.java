package defineoutside.listener;

import defineoutside.creator.DefinePlayer;
import defineoutside.main.GameManager;
import defineoutside.main.Matchmaking;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class PlayerJoinListener implements Listener {

    // TODO: Find the best game to join, not just an available game.
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        GameManager gm = new GameManager();
        event.getPlayer().teleport(gm.getGameFromWorld(Bukkit.getWorld("world")).joinServerLocation);

        // Creates the player object
        DefinePlayer dp = new DefinePlayer();
        dp.createPlayer(event.getPlayer().getUniqueId());

        // Finds the player a game, or a lobby
        Matchmaking mm = new Matchmaking();
        mm.addPlayer(event.getPlayer().getUniqueId(), "lobby");
    }
}
