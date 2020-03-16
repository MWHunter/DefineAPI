package defineoutside.listener;

import defineoutside.creator.DefinePlayer;
import defineoutside.main.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class VehicleExitListener implements Listener {
    @EventHandler
    public void onPlayerAttemptUnfreeze(VehicleExitEvent event) {
        PlayerManager pm = new PlayerManager();
        if (event.getExited() instanceof Player) {
            DefinePlayer dp = pm.getDefinePlayer(event.getExited().getUniqueId());

            if (dp != null && dp.isFrozen()) {
                event.setCancelled(true);
            }
        }
    }
}
