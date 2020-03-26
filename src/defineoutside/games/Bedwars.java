package defineoutside.games;

import defineoutside.creator.DefinePlayer;
import defineoutside.creator.DefineTeam;
import defineoutside.creator.Game;
import defineoutside.main.ConfigManager;
import defineoutside.main.GameManager;
import defineoutside.main.MainAPI;
import defineoutside.main.PlayerManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Bedwars extends Game implements Listener {
    List<Location> objectives = new ArrayList<>();
    List<Block> playerBlocks = new ArrayList<>();
    // Below is a line from the Game class
    //String[] teamNames = new String[]{"Red", "Blue", "Lime", "Yellow", "White", "Orange", "Magenta", "Light Blue", "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Green", "Black", "Brown"};
    Material[] bedTypes = new Material[]{Material.RED_BED, Material.BLUE_BED, Material.LIME_BED, Material.YELLOW_BED, Material.WHITE_BED, Material.ORANGE_BED, Material.MAGENTA_BED,
            Material.LIGHT_BLUE_BED, Material.PINK_BED, Material.GRAY_BED, Material.LIGHT_GRAY_BED, Material.CYAN_BED, Material.PURPLE_BED, Material.GREEN_BED, Material.BLACK_BED, Material.BROWN_BED};

    List<Location> bedLocations = new ArrayList<>();
    List<Location> catalystLocations = new ArrayList<>();
    List<Location> nuggetLocations = new ArrayList<>();

    HashMap<UUID, Integer> playersAndKills = new HashMap<>();
    HashMap<UUID, Integer> playersAndFinalKills = new HashMap<>();
    HashMap<UUID, Integer> playersAndBedEliminations = new HashMap<>();

    @Override
    public void createGameWorldAndRegisterGame() {
        setGameType("bedwars");
        setAllowBlockPlace(true);
        setAllowBlockBreak(true);
        setAllowBlockForm(true);
        setAllowBlockMelt(true);
        setAllowBlockSpread(true);
        setAllowExplosions(true);
        setAllowFluidFlow(true);
        setAllowLeafDecay(true);
        setAllowPlayerMoveOnJoin(false);

        Bukkit.getPluginManager().registerEvents(this, MainAPI.getPlugin());
        super.createGameWorldAndRegisterGame();
    }

    @Override
    public void start() {
        super.start();

        bedLocations = ConfigManager.getSpecialPositionsList(getWorldFolder().getName(), "bed");
        catalystLocations = ConfigManager.getSpecialPositionsList(getWorldFolder().getName(), "catalyst");
        nuggetLocations = ConfigManager.getSpecialPositionsList(getWorldFolder().getName(), "nugget");

        // Bed spawning logic
        for (Location bed : bedLocations) {
            int bedInt = bedLocations.indexOf(bed);
            Material bedType = bedTypes[bedInt];

            double x = bed.getX();
            double z = bed.getZ();

            World gameWorld = getGameWorld().getBukkitWorld();
            Block formerBlock = gameWorld.getBlockAt(bed);
            formerBlock.setType(Material.WHITE_BED);

            BlockFace bedFace;

            // Find direction of the bed
            if (Math.abs(x) > Math.abs(z)) {
                if (x > 0) {
                    // Bed facing west
                    bedFace = BlockFace.EAST;
                } else {
                    // Bed facing east
                    bedFace = BlockFace.WEST;
                }
            } else {
                if (z > 0) {
                    // Bed facing south
                    bedFace = BlockFace.SOUTH;
                } else {
                    // Bed facing north
                    bedFace = BlockFace.NORTH;
                }
            }

            setBed(formerBlock, bedFace, bedType);
        }
    }

    @Override
    public void setScoreBoard(Player player) {
        DefinePlayer definePlayer = PlayerManager.getDefinePlayer(player.getUniqueId());

        definePlayer.createScoreboard("Bedwars", ChatColor.AQUA + "Bedwars");

        definePlayer.addObjective("topblank", ChatColor.BOLD + "", 13);

        definePlayer.addObjective("objective", ChatColor.AQUA + "Catalyst in: 0:00", 12);

        definePlayer.addObjective("rankblank", ChatColor.AQUA + "", 11);

        // Capitalization on names matters
        definePlayer.addObjective("Red", ChatColor.RED + "✗ " + ChatColor.RED + "Red", 10);
        definePlayer.addObjective("Blue", ChatColor.RED + "✗ " + ChatColor.BLUE + "Blue", 9);
        definePlayer.addObjective("Lime", ChatColor.RED + "✗ " + ChatColor.GREEN + "Lime", 8);
        definePlayer.addObjective("Yellow", ChatColor.RED + "✗ " + ChatColor.YELLOW + "Yellow", 7);

        definePlayer.addObjective("teamsblank", ChatColor.GREEN + "", 6);

        definePlayer.addObjective("kills", ChatColor.WHITE + "Kills: " + ChatColor.AQUA + "0", 5);

        definePlayer.addObjective("finalkills", ChatColor.WHITE + "Final Kills: " + ChatColor.AQUA + "0", 4);

        definePlayer.addObjective("beds", ChatColor.WHITE + "Broken Beds: " + ChatColor.AQUA + "0", 3);

        definePlayer.addObjective("bedsblank", ChatColor.WHITE + "", 2);

        definePlayer.addObjective("divider", ChatColor.WHITE + "" + ChatColor.BOLD + "-------------", 1);

        definePlayer.addObjective("servername", ChatColor.GREEN + "play.abyssmc.org", 0);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (definePlayer.getScoreboardName().equals("Bedwars")) {
                        // Calculations for what teams are alive
                        for (DefineTeam team : uuidDefineTeams) {
                            if (team.getUuidInTeam().size() > 0) {
                                if (PlayerManager.getDefinePlayer(team.getUuidInTeam().get(0)).getCanInfiniteRespawn()) {
                                    definePlayer.updateObjective(team.getName(), ChatColor.GREEN + "✔ " + teamColors[ArrayUtils.indexOf(teamNames, team.getName())] + team.getName());

                                } else {
                                    Integer alivePlayers = 0;
                                    for (UUID playerUUID : team.getUuidInTeam()) {
                                        if (PlayerManager.getDefinePlayer(playerUUID).isAlive()) {
                                            alivePlayers++;
                                        }
                                    }

                                    definePlayer.updateObjective(team.getName(), ChatColor.DARK_RED + "✗ " +
                                            teamColors[ArrayUtils.indexOf(teamNames, team.getName())] + team.getName() + " " + ChatColor.WHITE + alivePlayers + " alive");
                                }

                            } else {
                                definePlayer.updateObjective(team.getName(), ChatColor.DARK_RED + "✗ " + teamColors[ArrayUtils.indexOf(teamNames, team.getName())]);
                            }
                        }

                        // Update player kills, final kills, and bed eliminations
                        definePlayer.updateObjective("kills", ChatColor.WHITE + "Kills: " + ChatColor.AQUA + playersAndKills.get(player.getUniqueId()));
                        definePlayer.updateObjective("finalkills", ChatColor.WHITE + "Final Kills: " + ChatColor.AQUA +  playersAndFinalKills.get(player.getUniqueId()));
                        definePlayer.updateObjective("beds", ChatColor.WHITE + "Broken Beds: " + ChatColor.AQUA + playersAndBedEliminations.get(player.getUniqueId()));
                    } else {
                        cancel();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    cancel();
                }
            }
        }.runTaskTimer(MainAPI.getPlugin(), 20L, 20L);
    }

    @Override
    public void playerLoad(UUID playerUUID) {
        super.playerLoad(playerUUID);

        DefinePlayer definePlayer = PlayerManager.getDefinePlayer(playerUUID);
        definePlayer.setCanInfiniteRespawn(true);

        playersAndKills.put(playerUUID, 0);
        playersAndFinalKills.put(playerUUID, 0);
        playersAndBedEliminations.put(playerUUID, 0);
    }

    // Code for setting what a bed is
    public void setBed(Block start, BlockFace facing, Material material) {
        // assert material is bed
        for (Bed.Part part : Bed.Part.values()) {
            start.setType(material);
            final Bed bedState = (Bed) start.getBlockData();
            bedState.setPart(part);
            bedState.setFacing(facing);
            start.setBlockData(bedState);
            start = start.getRelative(facing.getOppositeFace());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (GameManager.getGameFromWorld(event.getPlayer().getWorld()).equals(this)) {
            playerBlocks.add(event.getBlock());
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        GameManager gm = new GameManager();
        if (event.getBlock().getType().toString().contains("BED")) {
            Location brokenBlock = event.getBlock().getLocation();
            Location closestBed = closestAxisToLocation(event.getBlock().getLocation(), bedLocations);
            if (closestBed.getX() - brokenBlock.getX() < 2 && closestBed.getY() == brokenBlock.getY() && closestBed.getZ() - brokenBlock.getZ() < 2) {
                //bedLocations.remove(closestBed);
                Integer position = bedLocations.indexOf(closestBed);
                String teamName = teamNames[position];

                for (DefineTeam teams : uuidDefineTeams) {
                    if (teams.getName().equals(teamName)) {
                        if (!teams.getUuidInTeam().contains(event.getPlayer().getUniqueId())) {
                            for (UUID uuid : teams.getUuidInTeam()) {
                                PlayerManager.getDefinePlayer(uuid).setCanInfiniteRespawn(false);
                            }

                            event.getPlayer().sendMessage(ChatColor.WHITE + "You broke " + teamColors[position] + teamName + ChatColor.WHITE + "'s bed");
                        } else {
                            event.getPlayer().sendMessage(ChatColor.RED + "You cannot break your own bed");
                            event.setCancelled(true);
                        }
                    }
                }
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "You broke a bed not linked to any team!  (This shouldn't happen)");
            }

            event.setDropItems(false);
        }
    }

    // Stop people from defusing the bomb with a bomb
    @EventHandler
    public void onBlockExplosionEvent(EntityExplodeEvent event) {
        if (GameManager.getGameFromWorld(event.getEntity().getWorld()).equals(this)) {
            event.blockList().removeIf(blockList -> blockList.getType().toString().contains("BED"));
        }
    }

    public void checkObjectiveEnd() {
        if (objectives.size() <= 0) {
            doEndCountdown();
        }
    }
}
