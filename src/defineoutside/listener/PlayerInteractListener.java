package defineoutside.listener;

import defineoutside.main.ActionParser;
import defineoutside.main.GameManager;
import defineoutside.main.ItemTag;
import defineoutside.main.PlayerManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PlayerInteractListener implements Listener {

    // I think I should code special items like fireball and throwable tnt into this class, instead of in individual mini games
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        // For simplicity, check if player clicked with main hand, not off hand
        if (event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND)) {

            // Allow players to right click with items that do special things
            if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                ItemStack itemHeld = event.getPlayer().getInventory().getItemInMainHand();
                if (itemHeld.hasItemMeta()) {

                    String action = ItemTag.getTag(itemHeld, "DefineAPI");
                    ActionParser.doAction(PlayerManager.getDefinePlayer(event.getPlayer().getUniqueId()), action, ItemTag.getTag(itemHeld, action));
                }
            }
        }

        if (event.getAction().equals(Action.PHYSICAL) && event.getClickedBlock().getType().equals(Material.LIGHT_WEIGHTED_PRESSURE_PLATE)) {
            GameManager gm = new GameManager();
            if (gm.getGameFromWorld(event.getPlayer().getWorld()).isAllowGoldenLaunchpads()) {
                Player player = event.getPlayer();

                player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1, 1);

                Vector newVelocity = player.getLocation().getDirection().multiply(3.8D);
                newVelocity.setY(Math.abs(newVelocity.getY()) * 0.4);
                newVelocity.setY(newVelocity.getY() + 0.4);

                player.setVelocity(newVelocity);
            }
        }
    }
}
