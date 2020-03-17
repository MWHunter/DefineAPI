package defineoutside.listener;

import defineoutside.creator.DefinePlayer;
import defineoutside.main.GameManager;
import defineoutside.main.PlayerManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

public class DoubleJumpListener implements Listener {
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        GameManager gm = new GameManager();
        Player player = event.getPlayer();
        PlayerManager playerManager = new PlayerManager();
        DefinePlayer dp = playerManager.getDefinePlayer(player);

        if (gm.getGameFromWorld(player.getWorld()).allowPlayerDoubleJump) {
            event.setCancelled(true);
            player.setAllowFlight(false);
            player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1, 1);

            Vector newVelocity = player.getLocation().getDirection().multiply(1.6d);
            newVelocity.setY(Math.abs(newVelocity.getY()) * 0.8);
            newVelocity.setY(newVelocity.getY() + 0.35);

            player.setVelocity(newVelocity);
        }
    }
}
