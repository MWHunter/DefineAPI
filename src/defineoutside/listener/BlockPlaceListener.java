package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        GameManager gm = new GameManager();
        if (!gm.getGameFromWorld(event.getPlayer().getWorld()).isAllowBlockPlace()) {
            event.getPlayer().updateInventory();
            event.setCancelled(true);
        }
    }
}
