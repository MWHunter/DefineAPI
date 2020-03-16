package defineoutside.creator;

import defineoutside.main.MainAPI;
import defineoutside.main.VoidWorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;

public class DefineWorld {
    private World world;

    private boolean isReady = false;

    HashMap<String, Location> respawnMap = new HashMap();

    public void createArena(String uuid, File srcDir) {

        // Already loaded world
        if (uuid == "world" || uuid == "world_nether" || uuid == "world_the_end") {

            world = Bukkit.getWorld(uuid);
            isReady = true;

        } else {
            // Debug
            new BukkitRunnable() {
                File fullOutputDir = new File(
                        MainAPI.getPlugin().getServer().getWorldContainer() + File.separator + uuid);

                // Copy the files
                public void run() {
                    try {
                        // Worth a check, otherwise the server would get deleted if this was null
                        // Has happened before :(
                        if (uuid != null && fullOutputDir.exists()) {
                            FileUtils.cleanDirectory(new File(
                                    MainAPI.getPlugin().getServer().getWorldContainer() + File.separator + uuid));

                            FileUtils.deleteQuietly(new File(srcDir + File.separator + "uid.dat"));
                        }
                        FileUtils.copyDirectory(srcDir, fullOutputDir);

                    } catch (Exception e) {
                        // Directory doesn't exist
                        e.printStackTrace();
                        // return false;
                    }

                    // Load the world sync
                    new BukkitRunnable() {
                        public void run() {
                            WorldCreator wc = new WorldCreator(uuid);
                            wc.generator(new VoidWorldGenerator());
                            wc.generateStructures(false);

                            // Create a world with that name
                            Bukkit.getServer().createWorld(wc);

                            // TODO: Load things from a config
                            respawnMap.put("red", new Location(Bukkit.getWorld(uuid), 0, 70, 0));

                            world = Bukkit.getWorld(uuid);
                            isReady = true;

                            // Register the world in the defineworld
                            //gm.getWorldsHashMap().get(uuid).setBukkitWorld(Bukkit.getWorld(uuid));
                        }
                    }.runTask(MainAPI.getPlugin());
                }
            }.runTaskAsynchronously(MainAPI.getPlugin());
        }
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public World getBukkitWorld() {
        return world;
    }

    public HashMap<String, Location> getRespawnMap() {
        return respawnMap;
    }

    public void setRespawnMap(HashMap<String, Location> respawnMap) {
        this.respawnMap = respawnMap;
    }
}
