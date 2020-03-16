package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

public class BlockSpreadListener implements Listener {

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        GameManager gm = new GameManager();
        event.setCancelled(!gm.getGameFromWorld(event.getBlock().getWorld()).isAllowBlockSpread());
    }
}
