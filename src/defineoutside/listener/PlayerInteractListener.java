package defineoutside.listener;

import defineoutside.main.ActionParser;
import defineoutside.main.ItemTag;
import defineoutside.main.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    // I think I should code special items like fireball and throwable tnt into this class, instead of in individual mini games
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        // For simplicity, check if player clicked with main hand, not off hand
        if (event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND)) {
            PlayerManager pm = new PlayerManager();

            // Allow players to right click with items that do special things
            if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                ItemStack itemHeld = event.getPlayer().getInventory().getItemInMainHand();
                if (itemHeld.hasItemMeta()) {
                    ItemTag it = new ItemTag();
                    ActionParser ap = new ActionParser();

                    String action = it.getTag(itemHeld, "DefineAPI");
                    ap.doAction(event.getPlayer(), action, it.getTag(itemHeld, action));
                }
            }
        }
    }
}
