package defineoutside.games;

import defineoutside.creator.DefinePlayer;
import defineoutside.creator.DefineTeam;
import defineoutside.creator.Game;
import defineoutside.main.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

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
    List<Location> shopLocations = new ArrayList<>();

    HashMap<DefinePlayer, Integer> playersAndKills = new HashMap<>();
    HashMap<DefinePlayer, Integer> playersAndFinalKills = new HashMap<>();
    HashMap<DefinePlayer, Integer> playersAndBedEliminations = new HashMap<>();

    HashMap<String, Double> teamGoldProduction = new HashMap<>();

    int catalystSpawnInterval = 100000;
    int goldSpawnInterval = 100000;

    ItemStack gold;
    ItemStack catalyst;


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
        setAllowItemDrop(true);

        Bukkit.getPluginManager().registerEvents(this, MainAPI.getPlugin());
        super.createGameWorldAndRegisterGame();
    }

    @Override
    public void start() {
        super.start();

        bedLocations = ConfigManager.getSpecialPositionsList(getWorldFolder().getName(), "bed");
        catalystLocations = ConfigManager.getSpecialPositionsList(getWorldFolder().getName(), "catalyst");
        nuggetLocations = ConfigManager.getSpecialPositionsList(getWorldFolder().getName(), "nugget");
        shopLocations = ConfigManager.getSpecialPositionsList(getWorldFolder().getName(), "shop");

        catalystSpawnInterval = 30 + ((catalystLocations.size() - 1) * 15);
        goldSpawnInterval = 20;

        gold = new ItemStack(Material.GOLD_INGOT);
        ItemMeta itemMeta = gold.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "Gold");
        itemMeta.setLore(Arrays.asList(ChatColor.WHITE + "Instant gold!  Return it to a shopkeeper"));
        gold.setItemMeta(itemMeta);

        catalyst = new ItemStack(Material.NETHER_STAR);
        ItemMeta itemMeta2 = catalyst.getItemMeta();
        itemMeta2.setDisplayName(ChatColor.AQUA + "Catalyst");
        itemMeta2.setLore(Arrays.asList(ChatColor.WHITE + "Gives your team increased gold production, return it to the shopkeeper"));
        catalyst.setItemMeta(itemMeta2);


        // Bed spawning logic
        for (DefineTeam team : uuidDefineTeams) {
            //Bukkit.broadcastMessage(uuidDefineTeams.toString());
            Location bed = bedLocations.get(ArrayUtils.indexOf(teamNames, team.getName()));

            int bedInt = bedLocations.indexOf(bed);
            Material bedType = bedTypes[bedInt];

            double x = bed.getX();
            double z = bed.getZ();

            World gameWorld = getGameWorld().getBukkitWorld();
            Block formerBlock = gameWorld.getBlockAt(bed);

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

            // I'll just throw team gold production in here because why not?
            teamGoldProduction.put(team.getName(), 1.2D);
        }

        // Shop spawning logic
        for (Location shop : shopLocations) {

            shop.setWorld(getGameWorld().getBukkitWorld());

            double x = shop.getX();
            double z = shop.getZ();

            if (Math.abs(x) > Math.abs(z)) {
                if (z > 0) {
                    shop.setDirection(new Vector(0, 0, -1));
                } else {
                    shop.setDirection(new Vector(0, 0, 1));
                }
            } else {
                if (x > 0) {
                    shop.setDirection(new Vector(-1, 0, 0));
                } else {
                    shop.setDirection(new Vector(1, 0, 0));
                }
            }

            Villager villager = (Villager) shop.getWorld().spawnEntity(shop, EntityType.VILLAGER);
            villager.setVillagerType(Villager.Type.SNOW);
            villager.setProfession(Villager.Profession.TOOLSMITH);
            villager.setAI(false);
            villager.setInvulnerable(true);
            villager.setCollidable(false);
            villager.setGravity(false);
            villager.setCustomName("Right click for shop");
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                gameTime++;

                if (isGameEnding) {
                    cancel();
                }

                for (DefinePlayer inspectedPlayer : getUuidParticipating()) {

                    inspectedPlayer.setMoney(inspectedPlayer.getMoney() + teamGoldProduction.get(inspectedPlayer.getPlayerDefineTeam().getName()));
                }

                // Catalyst spawning logic
                if (catalystSpawnInterval - (gameTime % catalystSpawnInterval) == 1) {
                    for (Location location : catalystLocations) {


                        getGameWorld().getBukkitWorld().dropItem(location, catalyst).setVelocity(new Vector(0,0,0));
                    }
                }

                // Gold spawning logic
                if (goldSpawnInterval - (gameTime % goldSpawnInterval) == 1) {
                    for (Location location : nuggetLocations) {

                        getGameWorld().getBukkitWorld().dropItem(location, gold).setVelocity(new Vector(0,0,0));
                    }
                }
            }
        }.runTaskTimer(MainAPI.getPlugin(), 20, 20);

    }

    @Override
    public void setScoreBoard(Player player) {
        DefinePlayer definePlayer = PlayerManager.getDefinePlayer(player.getUniqueId());

        definePlayer.createScoreboard("Bedwars", ChatColor.AQUA + "Bedwars");

        definePlayer.addObjective("topblank", ChatColor.BOLD + "", 13);

        definePlayer.addObjective("objective", ChatColor.WHITE + "Catalyst in: " + ChatColor.AQUA + "0", 12);

        definePlayer.addObjective("rankblank", ChatColor.AQUA + "", 11);

        // Capitalization on names matters
        definePlayer.addObjective("Red", ChatColor.RED + "✗ " + ChatColor.RED + "Red", 10);
        definePlayer.addObjective("Blue", ChatColor.RED + "✗ " + ChatColor.BLUE + "Blue", 9);
        definePlayer.addObjective("Lime", ChatColor.RED + "✗ " + ChatColor.GREEN + "Lime", 8);
        definePlayer.addObjective("Yellow", ChatColor.RED + "✗ " + ChatColor.YELLOW + "Yellow", 7);

        definePlayer.addObjective("teamsblank", ChatColor.GREEN + "", 6);

        definePlayer.addObjective("gold", ChatColor.WHITE + "Gold: " + ChatColor.GOLD + "0", 5);

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
                                if (team.getUuidInTeam().get(0).getCanInfiniteRespawn()) {
                                    definePlayer.updateObjective(team.getName(), ChatColor.GREEN + "✔ " + teamColors[ArrayUtils.indexOf(teamNames, team.getName())] + team.getName());

                                } else {
                                    Integer alivePlayers = 0;
                                    for (DefinePlayer definePlayer1 : team.getUuidInTeam()) {
                                        if (definePlayer1.isAlive()) {
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
                        // Check for dividing by zero.
                        // minus one because scoreboard is delayed by a second, and modulus reports to 1, and not 0.
                        //Bukkit.broadcastMessage("Scoreboard says" + (catalystSpawnInterval - (gameTime % catalystSpawnInterval) - 1));
                        if (catalystLocations.size() != 0) {
                            definePlayer.updateObjective("objective", ChatColor.WHITE + "Catalyst in: " + (catalystSpawnInterval - (gameTime % catalystSpawnInterval) - 1));
                        }
                        definePlayer.updateObjective("gold", ChatColor.WHITE + "Gold: " + ChatColor.GOLD + (int) definePlayer.getMoney());
                        definePlayer.updateObjective("kills", ChatColor.WHITE + "Kills: " + ChatColor.AQUA + playersAndKills.get(definePlayer));
                        definePlayer.updateObjective("finalkills", ChatColor.WHITE + "Final Kills: " + ChatColor.AQUA + playersAndFinalKills.get(definePlayer));
                        definePlayer.updateObjective("beds", ChatColor.WHITE + "Broken Beds: " + ChatColor.AQUA + playersAndBedEliminations.get(definePlayer));
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
    public void playerLoad(DefinePlayer definePlayer) {
        super.playerLoad(definePlayer);

        definePlayer.setCanInfiniteRespawn(true);
        definePlayer.setMoney(500);

        playersAndKills.put(definePlayer, 0);
        playersAndFinalKills.put(definePlayer, 0);
        playersAndBedEliminations.put(definePlayer, 0);
    }

    @Override
    public void sendKillMessage(Player playerKilled, Player killer) {
        DefinePlayer defineKilled = PlayerManager.getDefinePlayer(playerKilled.getUniqueId());
        DefinePlayer defineKiller = PlayerManager.getDefinePlayer(killer.getUniqueId());

        if (defineKilled.getCanInfiniteRespawn()) { // Not final kill
            String killMessage = ChatColor.GRAY + "Death > " + ChatColor.WHITE + killMessageStrings[random.nextInt(killMessageStrings.length)] + ChatColor.RED + playerKilled.getName();

            playerKilled.sendMessage(ChatColor.GRAY + "Death > " + ChatColor.WHITE + "You were killed by " + ChatColor.RED + killer.getName() + ChatColor.WHITE +
                    " with " + ChatColor.RED + (int) Math.round(killer.getHealth() * 10d) / 20d + "♥" + ChatColor.WHITE + " remaining");
            playerKilled.sendMessage(ChatColor.GRAY + "Game > " + ChatColor.WHITE + "You have lost " + ChatColor.GOLD + (int)(defineKilled.getMoney() * 0.2) + ChatColor.WHITE + " gold");
            killer.sendMessage(ChatColor.GRAY + "Game > " + ChatColor.WHITE + "You have stolen " + ChatColor.GOLD + (int)(defineKilled.getMoney() * 0.2) + ChatColor.WHITE + " gold");

            // Don't tilt the killed player by saying they got rekt
            for (DefinePlayer messagePlayer : uuidParticipating) {
                if (!messagePlayer.getBukkitPlayer().equals(playerKilled)) {
                    messagePlayer.getBukkitPlayer().sendMessage(killMessage);
                }
            }

            defineKiller.setMoney(defineKilled.getMoney() * 0.2 + defineKiller.getMoney());
            defineKilled.setMoney(defineKilled.getMoney() * 0.8);

        } else { // Final kill
            String killMessage = ChatColor.GRAY + "Death > " + ChatColor.WHITE + killMessageStrings[random.nextInt(killMessageStrings.length)] + ChatColor.RED + playerKilled.getName()
                    + ChatColor.WHITE + " as a final kill";

            playerKilled.sendMessage(ChatColor.GRAY + "Death > " + ChatColor.WHITE + "You were final killed by " + ChatColor.RED + killer.getName() + ChatColor.WHITE +
                    " with " + ChatColor.RED + (int) Math.round(killer.getHealth() * 10d) / 20d + "♥" + ChatColor.WHITE + " remaining");
            playerKilled.sendMessage(ChatColor.GRAY + "Game > " + ChatColor.WHITE + "You have lost " + ChatColor.GOLD + (int)(defineKilled.getMoney()) + ChatColor.WHITE + " gold");
            killer.sendMessage(ChatColor.GRAY + "Game > " + ChatColor.WHITE + "You have stolen " + ChatColor.GOLD + (int)(defineKilled.getMoney()) + ChatColor.WHITE + " gold");

            // Don't tilt the killed player by saying they got rekt
            for (DefinePlayer messagePlayer : uuidParticipating) {
                if (!messagePlayer.getBukkitPlayer().equals(playerKilled)) {
                    messagePlayer.getBukkitPlayer().sendMessage(killMessage);
                }
            }

            defineKiller.setMoney(defineKilled.getMoney() + defineKiller.getMoney());
            defineKilled.setMoney(0);
        }


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
        if (event.getBlock().getType().toString().contains("BED")) {
            Location brokenBlock = event.getBlock().getLocation();
            Location closestBed = closestAxisToLocation(event.getBlock().getLocation(), bedLocations);
            if (closestBed.getX() - brokenBlock.getX() < 2 && closestBed.getY() == brokenBlock.getY() && closestBed.getZ() - brokenBlock.getZ() < 2) {
                int position = bedLocations.indexOf(closestBed);
                String teamName = teamNames[position];

                for (DefineTeam teams : uuidDefineTeams) {
                    if (teams.getName().equals(teamName)) {
                        if (!teams.getUuidInTeam().contains(PlayerManager.getDefinePlayer(event.getPlayer().getUniqueId()))) {
                            for (DefinePlayer definePlayer : teams.getUuidInTeam()) {
                                definePlayer.setCanInfiniteRespawn(false);
                                definePlayer.sendMessage(ChatColor.RED + "You can no longer respawn");
                            }

                            messageGamePlayers(ChatColor.WHITE + event.getPlayer().getName() + " broke " + teamColors[position] + teamName + ChatColor.WHITE + "'s bed");
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

    @EventHandler
    public void onPlayerInteractVillager(PlayerInteractEntityEvent event) {
        Entity villager = event.getRightClicked();
        if (villager instanceof Villager) {
            if (GameManager.getGameFromWorld(villager.getWorld()).equals(this)) {
                Player player = event.getPlayer();
                DefinePlayer definePlayer = PlayerManager.getDefinePlayer(player.getUniqueId());

                ActionParser.doAction(definePlayer, "inventory", "bedwars.yml");
                event.setCancelled(true);

                if (player.getInventory().contains(Material.GOLD_INGOT)) {
                    int amountOfGold = 0;
                    for (ItemStack inventoryStack : player.getInventory().getContents()) {
                        if (inventoryStack != null && inventoryStack.getType().equals(Material.GOLD_INGOT)) {
                            amountOfGold += inventoryStack.getAmount();
                        }
                    }

                    player.getInventory().remove(Material.GOLD_INGOT);

                    player.sendMessage(ChatColor.WHITE + "You received " + ChatColor.GOLD + amountOfGold * 50 + " gold" + ChatColor.WHITE + " from " + amountOfGold + " ingots");
                    definePlayer.setMoney(definePlayer.getMoney() + amountOfGold * 50);
                }

                if (player.getInventory().contains(Material.NETHER_STAR)) {
                    String teamName = PlayerManager.getDefinePlayer(player.getUniqueId()).getPlayerDefineTeam().getName();
                    Double teamGold = teamGoldProduction.get(teamName);

                    int amountOfCatalyst = 0;
                    for (ItemStack inventoryStack : player.getInventory().getContents()) {
                        if (inventoryStack != null && inventoryStack.getType().equals(Material.NETHER_STAR)) {
                            amountOfCatalyst += inventoryStack.getAmount();
                        }
                    }

                    player.getInventory().remove(Material.NETHER_STAR);

                    int teamGoldIncreased = (int)((teamGold * Math.pow(1.15, amountOfCatalyst)) * 100);

                    player.sendMessage(ChatColor.WHITE + "Team gold production increased to " + teamGoldIncreased/100D);

                    teamGoldProduction.put(teamName, teamGoldIncreased / 100D);
                }
            }
        }
    }
}
