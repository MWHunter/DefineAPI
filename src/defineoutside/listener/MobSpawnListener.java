package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobSpawnListener implements Listener {

    @EventHandler
    public void mobSpawnListener(CreatureSpawnEvent event) {
        try {
            CreatureSpawnEvent.SpawnReason[] spawnReasons = GameManager.getGameFromWorld(event.getLocation().getWorld()).getAllowedSpawnReasons();

            for (CreatureSpawnEvent.SpawnReason reason : spawnReasons) {
                if (reason.equals(event.getSpawnReason())) {
                    return;
                }
            }

            event.setCancelled(true);
        } catch (Exception e) {
            // This just means the world wasn't loaded yet and a plugin teleported just as it was loaded.
        }
    }
}
