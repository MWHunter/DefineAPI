package defineoutside.listener;

import defineoutside.creator.DefinePlayer;
import defineoutside.main.GameManager;
import defineoutside.main.MainAPI;
import defineoutside.main.Matchmaking;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class PlayerJoinListener implements Listener {

    // TODO: Find the best game to join, not just an available game.
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().teleport(GameManager.getGameFromWorld(Bukkit.getWorld("world")).getJoinServerLocation());

        // Creates the player object
        DefinePlayer dp = new DefinePlayer();
        dp.createPlayer(event.getPlayer().getUniqueId());

        // Finds the player a game, or a lobby
        Matchmaking mm = new Matchmaking();
        if (MainAPI.lobbyType.equalsIgnoreCase("lobby")) {
            mm.addPlayer(dp, "lobby");
        } else {
            mm.addPlayer(dp, "gamelobby");
        }
    }
}
