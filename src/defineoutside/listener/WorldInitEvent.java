package defineoutside.listener;

import defineoutside.main.MainAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldInitEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void worldInit(org.bukkit.event.world.WorldInitEvent e) {
        // This is called to try to prevent lag when loading the world.
        e.getWorld().setKeepSpawnInMemory(false);
        e.getWorld().setAutoSave(false);
    }

    @EventHandler
    public void worldLoad(WorldLoadEvent event) {
        if (event.getWorld().getName().equalsIgnoreCase("world")) {
            MainAPI.loadPostWorld();
        }
    }
}
