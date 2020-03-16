package defineoutside.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class WorldInitEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void worldInit(org.bukkit.event.world.WorldInitEvent e) {
        // This is called to try to prevent lag when loading the world.
        e.getWorld().setKeepSpawnInMemory(false);
        e.getWorld().setAutoSave(false);
    }
}
