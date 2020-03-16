package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

public class BlockFormListener implements Listener {

    @EventHandler
    public void blockFormListener(BlockFormEvent event) {
        GameManager gm = new GameManager();
        event.setCancelled(!gm.getGameFromWorld(event.getBlock().getWorld()).isAllowBlockForm());
    }
}
