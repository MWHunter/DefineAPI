package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class PlayerShootProjectile implements Listener {
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        GameManager gm = new GameManager();
        event.setCancelled(!gm.getGameFromWorld(event.getEntity().getWorld()).isAllowPlayerShootProjectile());
    }
}
