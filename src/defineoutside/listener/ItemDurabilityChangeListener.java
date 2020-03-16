package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class ItemDurabilityChangeListener implements Listener {

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        GameManager gm = new GameManager();
        event.setCancelled(!gm.getGameFromWorld(event.getPlayer().getWorld()).isAllowDurability());
    }
}
