package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodChangeListener implements Listener {

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        GameManager gm = new GameManager();

        event.setCancelled(!gm.getGameFromWorld(event.getEntity().getWorld()).isAllowHunger());
    }
}
