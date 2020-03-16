package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class PlayerPortalListener implements Listener {
    @EventHandler
    public void playerPortal(PlayerPortalEvent event) {
        GameManager gm = new GameManager();
        event.setCancelled(!gm.getGameFromWorld(event.getPlayer().getWorld()).isAllowPlayerPortal());
    }
}
