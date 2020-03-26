package defineoutside.listener;

import defineoutside.creator.Game;
import defineoutside.main.GameManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftingListener implements Listener {
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        Game game = GameManager.getGameFromWorld(event.getViewers().get(0).getWorld());

        if (!game.isAllowCrafting()) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }
}
