package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

public class BlockMeltListener implements Listener {

    @EventHandler
    public void blockMeltListener(BlockFadeEvent event) {
        GameManager gm = new GameManager();
        event.setCancelled(!gm.getGameFromWorld(event.getBlock().getWorld()).isAllowBlockMelt());
    }
}
