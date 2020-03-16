package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        GameManager gm = new GameManager();

        event.setCancelled(!gm.getGameFromWorld(event.getPlayer().getWorld()).isAllowBlockBreak());
    }
}
