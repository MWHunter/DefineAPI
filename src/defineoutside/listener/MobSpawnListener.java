package defineoutside.listener;

import defineoutside.main.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.Arrays;
import java.util.List;

public class MobSpawnListener implements Listener {

    @EventHandler
    public void mobSpawnListener(CreatureSpawnEvent event) {
        GameManager gm = new GameManager();

        List<SpawnReason> list = Arrays.asList(gm.getGameFromWorld(event.getEntity().getWorld()).getAllowedSpawnReasons());

        event.setCancelled(!list.contains(event.getSpawnReason()));
    }
}
