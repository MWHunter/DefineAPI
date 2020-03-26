package defineoutside.games;

import defineoutside.creator.Game;
import defineoutside.main.ConfigManager;
import defineoutside.main.GameManager;
import defineoutside.main.MainAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

public class Defuse extends Game implements Listener {
    List<Location> objectives = new ArrayList<>();

    @Override
    public void createGameWorldAndRegisterGame() {
        setGameType("defuse");
        setAllowBlockPlace(true);
        setAllowBlockBreak(true);
        setAllowBlockForm(true);
        setAllowBlockMelt(true);
        setAllowBlockSpread(true);
        setAllowExplosions(true);
        setAllowFluidFlow(true);
        setAllowLeafDecay(true);

        ConfigManager cm = new ConfigManager();
        for (Location location : cm.getSpecialPositionsList(getWorldFolder().getName(), "bomb")) {
            objectives.add(location);
        }

        Bukkit.getPluginManager().registerEvents(this, MainAPI.getPlugin());
        super.createGameWorldAndRegisterGame();
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        GameManager gm = new GameManager();
        if (event.getBlock().getType().equals(Material.REDSTONE_ORE) && gm.getGameFromWorld(event.getPlayer().getWorld()).equals(this)) {
            for (Location bombLocation : objectives) {
                if (event.getBlock().getX() == bombLocation.getX() && event.getBlock().getY() == bombLocation.getY() && event.getBlock().getZ() == bombLocation.getZ()) {
                    objectives.remove(bombLocation);

                    if (objectives.size() == 0) {
                        messageGamePlayers(ChatColor.GRAY + "Game > " + ChatColor.WHITE + "The last bomb has been defused");
                    } else {
                        messageGamePlayers(ChatColor.GRAY + "Game > " + ChatColor.WHITE + "The bomb at " + ChatColor.RED + (int) bombLocation.getX() + ", " + (int) bombLocation.getY() + ", " +
                                (int) bombLocation.getZ() + ChatColor.WHITE + " has been defused");
                    }

                    break;
                }
            }

            checkEndByEliminations();
            checkObjectiveEnd();
        }
    }

    // Stop people from defusing the bomb with a bomb
    @EventHandler
    public void onBlockExplosionEvent(EntityExplodeEvent event) {
        GameManager gm = new GameManager();
        if (gm.getGameFromWorld(event.getEntity().getWorld()).equals(this)) {
            for (Location bombLocation : objectives) {
                for (Block blockList : event.blockList()) {
                    if (bombLocation.getX() == blockList.getX() && bombLocation.getY() == blockList.getY() && bombLocation.getZ() == blockList.getZ()) {
                        messageGamePlayers(ChatColor.GRAY + "Game > " + ChatColor.WHITE + " The bomb cannot be defused with TnT");
                        event.blockList().remove(blockList);
                        break;
                    }
                }
            }
        }
    }

    public void checkObjectiveEnd() {
        if (objectives.size() <= 0) {
            doEndCountdown();
        }
    }
}
