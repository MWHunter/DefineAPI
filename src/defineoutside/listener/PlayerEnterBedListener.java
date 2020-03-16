package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class PlayerEnterBedListener implements Listener {

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        GameManager gm = new GameManager();
        event.setCancelled(!gm.getGameFromWorld(event.getPlayer().getWorld()).isAllowEnterBed());
    }
}
