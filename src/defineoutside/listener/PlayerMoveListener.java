package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer().getLocation().getBlockY() < -10) {
            // If the player can't die, so they just get teleported back
            if (!GameManager.getGameFromWorld(event.getPlayer().getWorld()).isAllowPlayerDamage()) {
                // TODO: Configurable location
                event.getPlayer().teleport(new Location(event.getPlayer().getWorld(), 0, 70, 0));
            } else if (GameManager.getGameFromWorld(event.getPlayer().getWorld()).isVoidInstantKill()) {
                event.getPlayer().damage(1000);
            }
        }

        Block block = event.getPlayer().getWorld().getBlockAt(event.getPlayer().getLocation().subtract(0, 1, 0));

        //Bukkit.broadcastMessage(block.isLiquid() + " and is air? " + block.getType().equals(Material.AIR));
        if (!block.isLiquid() && !block.getType().equals(Material.AIR)) {
            if (GameManager.getGameFromWorld(event.getPlayer().getWorld()).allowPlayerDoubleJump) {
                event.getPlayer().setAllowFlight(true);
            }
        }
    }
}
