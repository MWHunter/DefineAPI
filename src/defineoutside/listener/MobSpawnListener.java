package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MobSpawnListener implements Listener {

    @EventHandler
    public void mobSpawnListener(CreatureSpawnEvent event) {
        SpawnReason[] spawnReasons =
                GameManager.
                        getGameFromWorld(
                                event.
                                        getEntity().
                                        getWorld())
                        .getAllowedSpawnReasons();

        for (SpawnReason reason : spawnReasons) {
            if (reason.equals(event.getSpawnReason())) {
                event.setCancelled(true);
            }
        }
    }
}
