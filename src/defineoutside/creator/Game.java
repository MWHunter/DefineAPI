package defineoutside.creator;

import defineoutside.games.GameLobby;
import defineoutside.main.*;
import io.papermc.lib.PaperLib;
import org.bukkit.*;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class Game implements Listener {
    private int maxPlayers = 999;
    private int teams = 0;
    public int gameCountdown = 10;
    public int gameCountdownDecrement = 5;
    private int gameCountdownAlwaysAnnounce = 4;
    private int gameEndCountdown = 5;
    private int maxGameTime = 30 * 60;
    private int respawnTime = 5;
    private int teleportIndex = 0;
    public int gameTime = 0;

    // Chat, combat, join, move, quit needs to be worked on
    private boolean allowBlockBreak = false;
    private boolean allowBlockForm = false;
    private boolean allowBlockMelt = false;
    private boolean allowBlockPlace = false;
    private boolean allowBlockSpread = false;
    private boolean allowEntityDamage = false;
    private boolean allowEntityGrief = false;
    private boolean allowExplosions = false;
    private boolean allowFluidFlow = false;
    private boolean allowHunger = false;
    private boolean allowItemDrop = false;
    private boolean allowDurability = false;
    private boolean allowLeafDecay = false;
    private boolean allowLightning = false;
    private boolean allowMobSpawn = false;
    private boolean allowEnterBed = false;
    private boolean allowInteractStorage = false;
    private boolean allowPlayerRespawn = false;
    private boolean allowPlayerPortal = false;
    private boolean allowWeatherChange = false;
    private boolean allowPlayerDamage = true;
    public boolean allowPlayerShootProjectile = false;
    private boolean allowGoldenLaunchpads = false;
    private boolean allowCrafting = false;

    private boolean allowPlayerMoveOnJoin = true;
    public boolean allowPlayerDoubleJump = false;

    private boolean voidInstantKill = true;

    public boolean allowPlayersToJoinNow = true;
    private boolean alwaysAllowPlayersJoin = false;
    private boolean generateTeams = false;

    public boolean isGameStarting = false;
    public boolean canGameStart = true;
    public boolean isGameEnding = false;
    private boolean hasStarted = false;

    // These two should be set by gamemanager
    private File worldFolder = null;

    private String gameType = "testwars";
    private String spawnKitName = "empty";

    //private String spawnKit = "default";

    SpawnReason[] allowedSpawnReasons = new SpawnReason[]{SpawnReason.CUSTOM, SpawnReason.SHEARED, SpawnReason.SHOULDER_ENTITY,
            SpawnReason.SLIME_SPLIT, SpawnReason.SILVERFISH_BLOCK, SpawnReason.SPAWNER_EGG};
    public String[] killMessageStrings = new String[]{"You rekt ", "You destroyed ", "You OwO'd ", "You stabbed ", "You misclicked on "};

    // 16 ready teams in total
    public String[] teamNames = new String[]{"Red", "Blue", "Lime", "Yellow", "White", "Orange", "Magenta", "Light Blue", "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Green", "Black", "Brown"};
    public Material[] teamWools = new Material[]{Material.RED_WOOL, Material.BLUE_WOOL, Material.LIME_WOOL, Material.YELLOW_WOOL, Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL,
            Material.LIGHT_BLUE_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.GREEN_WOOL, Material.BLACK_WOOL, Material.BROWN_WOOL};
    public ChatColor[] teamColors = new ChatColor[]{ChatColor.RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.WHITE, ChatColor.GOLD, ChatColor.DARK_PURPLE, ChatColor.AQUA, ChatColor.LIGHT_PURPLE,
            ChatColor.DARK_GRAY, ChatColor.GRAY, ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.GREEN, ChatColor.BLACK, ChatColor.WHITE};
    // TODO: Make colors from RGB instead of guessing what these stupid names means
    // What is Fuchsia?  I can't find any word that looks remotely like it?  Where did English steal this word from?
    public Color[] teamDyes = new Color[]{Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.WHITE, Color.ORANGE, Color.FUCHSIA, Color.TEAL, Color.RED, Color.BLACK, Color.GRAY,
            Color.OLIVE, Color.YELLOW, Color.MAROON, Color.GREEN, Color.BLACK, Color.NAVY};
    public ArrayList<Material> dyedItems = new ArrayList<>(Arrays.asList(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS));

    public ArrayList<DefinePlayer> uuidParticipating = new ArrayList<DefinePlayer>();
    public ArrayList<DefinePlayer> uuidSpectating = new ArrayList<DefinePlayer>();

    public ArrayList<DefineTeam> uuidDefineTeams = new ArrayList<>();

    public ItemStack[] spawnItemList = new ItemStack[41];

    private UUID gameUUID;

    DefineWorld gameWorld = new DefineWorld();

    public Random random = new Random();

    public Location joinServerLocation = new Location(Bukkit.getWorld("world"), 0, 70, 0);

    public void createGameWorldAndRegisterGame() {

        Bukkit.getLogger().log(Level.SEVERE, gameUUID + " is UUID");
        if (gameUUID == null) {
            gameUUID = UUID.randomUUID();
            Bukkit.getLogger().log(Level.SEVERE, gameUUID + " is chosen");
        }

        if (!generateTeams) {
            createTeams();
        }


        // TODO: Attempt to get world spawns
        if (getWorldFolder() == null) {
            setWorldFolder(new File("world"));
        }

        spawnItemList = ConfigManager.getRandomKit(gameType);

        GameManager.registerGame(gameUUID, this);
    }

    public void createTeams() {
        List<List<Location>> listOfListOfSpawns = ConfigManager.getListOfSpawns(getWorldFolder().getName());

        for (int i = 0; i < teams; i++) {
            //Bukkit.broadcastMessage("Team amount is " + teams + " we are on " + i);

            DefineTeam defineTeam = new DefineTeam();

            for (String teamName : teamNames) {
                boolean isUniqueName = true;

                for (DefineTeam teams : uuidDefineTeams) {
                    if (teams.getName().equals(teamName)) {
                        isUniqueName = false;
                    }
                }

                if (isUniqueName) {
                    defineTeam.setName(teamName);

                    defineTeam.setWoolType(teamWools[ArrayUtils.indexOf(teamNames, teamName)]);
                    defineTeam.setChatColor(teamColors[ArrayUtils.indexOf(teamNames, teamName)]);

                    break;
                }
            }

            defineTeam.setSpawns(listOfListOfSpawns.get(i));

            uuidDefineTeams.add(defineTeam);
        }
    }

    // Also, this should not be run by a lobby, override this!
    public void startGameCountdown() {
        if (canGameStart) {
            isGameStarting = true;
            setAllowPlayerDamage(false);

            new BukkitRunnable() {
                int count = gameCountdown;

                public void run() {
                    if (--count <= 0) {
                        start();
                        cancel();
                    } else {
                        if (!isGameEnding && (count % gameCountdownDecrement == 0 || count <= getGameCountdownAlwaysAnnounce())) {
                            messageGamePlayers(ChatColor.GRAY + "Game > " + ChatColor.WHITE + "This game will begin in " + ChatColor.RED + count + ChatColor.WHITE + " seconds");
                        } else if (isGameEnding) {
                            messageGamePlayers(ChatColor.RED + "Start has been canceled because a player left the game");
                            cancel();
                        }
                    }
                }
            }.runTaskTimer(MainAPI.getPlugin(), 20L, 20L);
        }

        canGameStart = false;
        allowPlayersToJoinNow = false;
    }

    public void start() {
        hasStarted = true;
        allowPlayerMoveOnJoin = true;

        setAllowPlayerShootProjectile(true);
        setAllowPlayerDamage(true);

        for (DefinePlayer definePlayer : getUuidParticipating()) {
            Player player = definePlayer.getBukkitPlayer();

            player.sendMessage(ChatColor.GRAY + "Game > " + ChatColor.WHITE + "This game has begun. " + ChatColor.RED + "GLHF!");

            playerLoad(definePlayer);
            definePlayer.setFreeze(false);

            ItemStack[] array = ConfigManager.getKit(definePlayer.getKit()).clone();

            for (int i = 0; i < array.length; i++) {
                int randomIndexToSwap = random.nextInt(array.length);

                ItemStack temp = array[randomIndexToSwap];
                array[randomIndexToSwap] = array[i];
                array[i] = temp;
            }

            for (ItemStack items : array) {
                if (!ItemTag.getTag(items, "Kit").equals("")) {
                    definePlayer.setKit(ItemTag.getTag(items, "Kit"));
                    break;
                }
            }

            givePlayerKit(player, ConfigManager.getKit(definePlayer.getKit()));
        }
    }

    public void checkEndByEliminations() {

        if (isGameEnding) {
            return;
        }

        // avoid null pointers
        if (getUuidParticipating().size() > 0) {
            DefineTeam playerDefineTeam = null;

            // Logic for seeing if there are still multiple teams
            for (DefinePlayer dp : getUuidParticipating()) {
                if (dp.isAlive() || dp.playerDeathCanRespawn()) {
                    if (playerDefineTeam == null) {
                        playerDefineTeam = dp.getPlayerDefineTeam();
                    } else if (!dp.getPlayerDefineTeam().equals(playerDefineTeam)) {
                        return;
                    }
                }
            }
        }

        doEndCountdown();
    }

    public void doEndCountdown() {
        if (!isGameEnding) {
            messageGamePlayers(ChatColor.GRAY + "Game > " + ChatColor.WHITE + "This game will end in " + ChatColor.RED + gameEndCountdown + ChatColor.WHITE + " seconds");

            isGameEnding = true;
            allowPlayersToJoinNow = false;

            // No players, or everyone is one one team.  End the game after the countdown
            new BukkitRunnable() {
                public void run() {
                    end();
                }
            }.runTaskLater(MainAPI.getPlugin(), gameEndCountdown * 20L);
        }
    }

    // Find a better game for everyone, kick everyone out, and delete this game.
    public void end() {

        // Garbage collection takes care of this current object once this is called
        // RIP game object :(
        GameManager.deleteWorld(gameUUID);
        GameManager.deleteGame(gameUUID);

        HandlerList.unregisterAll(this);

        // Find a gamelobby for players to join
        for (Game game : GameManager.getGamesHashMap().values()) {
            if (game instanceof GameLobby) {
                ((GameLobby) game).setLobbyForGametype(getGameType());
                game.setCanGameStart(true);
                GameManager.transferPlayers(getUuidParticipating(), game);

                break;
            }
        }

        new BukkitRunnable() {
            World unloadWorld = getGameWorld().getBukkitWorld();

            public void run() {
                Bukkit.unloadWorld(unloadWorld, false);
            }
        }.runTaskLater(MainAPI.getPlugin(), 600L);

        // And then delete the world folder, async
        new BukkitRunnable() {
            public void run() {
                try {
                    FileUtils.deleteDirectory(new File(Bukkit.getWorldContainer() + File.separator + gameUUID));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLaterAsynchronously(MainAPI.getPlugin(), 1200L);
    }

    public void playerLoad(DefinePlayer definePlayer) {
        // This should be changed when this class becomes generic
        definePlayer.reset();
        definePlayer.setCanInfiniteRespawn(false);
        definePlayer.setInGameType(gameType);
        definePlayer.setKit(getSpawnKitName());

        playerRespawn(definePlayer, allowPlayerMoveOnJoin, !hasStarted);
    }

    public void playerRespawn(DefinePlayer definePlayer) {
        playerRespawn(definePlayer, true);
    }

    public void playerRespawn(DefinePlayer definePlayer, boolean canMove) {
        playerRespawn(definePlayer, canMove, false);
    }

    public void playerRespawn(DefinePlayer definePlayer, boolean canMove, boolean kitSelector) {
        ConfigManager cm = new ConfigManager();
        Player bukkitPlayer = definePlayer.getBukkitPlayer();

        // This will null pointer if the player leaves the same tick as the game starts
        try {
            bukkitPlayer.setGameMode(GameMode.SURVIVAL);
            bukkitPlayer.setHealth(20);
            bukkitPlayer.getActivePotionEffects().clear();
            bukkitPlayer.setExp(0);

            bukkitPlayer.setAllowFlight(allowPlayerDoubleJump);
        } catch (NullPointerException e) {
            MainAPI.getPlugin().getLogger().log(Level.WARNING, "Player left the same tick as the game started or as the player respawned!  Ignoring the respawn!");
        }

        ItemStack[] giveItems;
        if (kitSelector) {
            giveItems = cm.getRandomKitSelector(getGameType(), getWorldFolder().getName(), ArrayUtils.indexOf(teamNames, definePlayer.getPlayerDefineTeam().getName()));
        } else {
            giveItems = cm.getKit(definePlayer.getKit());
        }

        givePlayerKit(bukkitPlayer, giveItems);

        if (!definePlayer.isFrozen()) {
            // Check if the player has already been teleported to a valid spawn position
            Location teleportLocation = definePlayer.getPlayerDefineTeam().getNextSpawn();
            // Config is loaded before we have the world ready, so we must set world
            teleportLocation.setWorld(getGameWorld().getBukkitWorld());

            // Set the player to be facing towards 0,0 since it looks nicer (pun)
            Vector lookDirection = new Vector();
            lookDirection.setX(teleportLocation.getX() * -1);
            lookDirection.setY(0);
            lookDirection.setZ(teleportLocation.getZ() * -1);

            teleportLocation.setDirection(lookDirection);

            PaperLib.teleportAsync(bukkitPlayer, teleportLocation).thenAccept(result -> {
                if (result) {
                    if (canMove == false) {
                        definePlayer.setFreeze(true);
                    }
                } else {
                    bukkitPlayer.sendMessage(ChatColor.RED + "Something went wrong while teleporting you to the game.  Recovering by sending you back to the hub");
                    Matchmaking mm = new Matchmaking();
                    mm.addPlayerToCentralQueue(definePlayer, "lobby");
                }
            });
        } else {
            definePlayer.setFreeze(false);
        }
    }

    public void givePlayerKit(Player player, ItemStack[] giveItems) {
        player.getInventory().clear();

        for (int i = 0; i < 41; i++) {
            ItemStack givenItem = giveItems[i];

            if (dyedItems.contains(givenItem.getType())) {
                LeatherArmorMeta meta = (LeatherArmorMeta) givenItem.getItemMeta();
                DefinePlayer definePlayer = PlayerManager.getDefinePlayer(player.getUniqueId());
                meta.setColor(teamDyes[ArrayUtils.indexOf(teamNames, definePlayer.getPlayerDefineTeam().getName())]);
                givenItem.setItemMeta(meta);
            }
            player.getInventory().setItem(i, givenItem);
        }
    }

    public void playerJoin(DefinePlayer definePlayer) {

        String joinMessage = ChatColor.GRAY + "Game > " + ChatColor.RED + definePlayer.getName() + ChatColor.WHITE + " has joined the game";

        // Null means no game lobby set this
        if (definePlayer.getPlayerDefineTeam() == null) {
            definePlayer.setPlayerDefineTeam(getBestTeam());

            DefineTeam joinerDefineTeam = definePlayer.getPlayerDefineTeam();

            joinMessage = ChatColor.GRAY + "Game > " + ChatColor.RED + definePlayer.getName() + ChatColor.WHITE + " has joined the " +
                    joinerDefineTeam.getChatColor() + joinerDefineTeam.getName() + ChatColor.WHITE + " team";

            messageGamePlayers(joinMessage);
        }

        definePlayer.setKit(spawnKitName);

        uuidParticipating.add(definePlayer);

        setScoreBoard(definePlayer.getBukkitPlayer());
        playerLoad(definePlayer);

        attemptStart();
    }

    public void playerLeave(DefinePlayer player) {
        uuidParticipating.remove(player);

        for (DefinePlayer definePlayer : getUuidParticipating()) {
            definePlayer.getBukkitPlayer().sendMessage(ChatColor.GRAY + "Game > " + ChatColor.RED + player.getName() + ChatColor.WHITE + " has left the game");
        }

        player.getPlayerDefineTeam().removePlayer(player);

        checkEndByEliminations();
    }

    public void attemptStart() {
        //Bukkit.broadcastMessage(uuidParticipating.size() + " is current playercount min is " + minPlayers + " " + !isGameStarting + " " + canGameStart);
        if (uuidParticipating.size() >= GameManager.getMinPlayers(gameType) && !isGameStarting && canGameStart) {
            startGameCountdown();
        }
    }

    public void sendKillMessage(Player playerKilled, Player killer) {
        playerKilled.sendMessage(ChatColor.GRAY + "Death > " + ChatColor.WHITE + "You were killed by " + ChatColor.RED + killer.getName() + ChatColor.WHITE +
                " with " + ChatColor.RED + (int) Math.round(killer.getHealth() * 10d) / 20d + "♥" + ChatColor.WHITE + " remaining");
        killer.sendMessage(ChatColor.GRAY + "Death > " + ChatColor.WHITE + killMessageStrings[random.nextInt(killMessageStrings.length)] + ChatColor.RED + playerKilled.getName());
    }

    public void playerDeath(DefinePlayer definePlayer, boolean canRespawn) {
        if (!isGameEnding) {
            Player player = definePlayer.getBukkitPlayer();

            player.setGameMode(GameMode.SPECTATOR);
            if (canRespawn) {

                player.sendMessage(ChatColor.AQUA + "You have died!  Respawning in " + ChatColor.WHITE + (respawnTime - 1) + ChatColor.AQUA + " seconds.");
                new BukkitRunnable() {
                    int currentSpawnTimer = respawnTime - 1;

                    public void run() {

                        player.sendMessage(ChatColor.AQUA + "Respawning in " + ChatColor.WHITE + --currentSpawnTimer + ChatColor.AQUA + " seconds.");
                        if (currentSpawnTimer <= 0) {
                            playerRespawn(definePlayer);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(MainAPI.getPlugin(), 20L, 20L);
            } else {
                player.sendMessage(ChatColor.AQUA + "You have died!  You will respawn in the next game");

                definePlayer.setAlive(false);
                checkEndByEliminations();
            }
        }
    }

    public DefineTeam getBestTeam() {
        int smallestTeam = Integer.MAX_VALUE;
        // As a backup, shouldn't be needed
        DefineTeam bestDefineTeam = new DefineTeam();

        for (DefineTeam defineTeam : uuidDefineTeams) {
            if (defineTeam.getSizeOfTeam() < smallestTeam) {
                smallestTeam = defineTeam.getSizeOfTeam();
                bestDefineTeam = defineTeam;
            }
        }

        return bestDefineTeam;
    }

    public void messageGamePlayers(String string) {
        for (DefinePlayer definePlayer : getUuidParticipating()) {
            definePlayer.getBukkitPlayer().sendMessage(string);
        }
    }

    public void setScoreBoard(Player player) {
        DefinePlayer definePlayer = PlayerManager.getDefinePlayer(player.getUniqueId());

        definePlayer.createScoreboard("Game", ChatColor.AQUA + "AbyssMC");

        definePlayer.addObjective("rank", ChatColor.GREEN + "" + ChatColor.BOLD + "Rank", 10);

        definePlayer.addObjective("rankvalue", ChatColor.WHITE + "Player", 9);

        definePlayer.addObjective("rankblank", ChatColor.AQUA + "", 8);

        definePlayer.addObjective("online", ChatColor.AQUA + "" + ChatColor.BOLD + "Online", 7);

        definePlayer.addObjective("totalplayers", ChatColor.WHITE + "" + MainAPI.getPlugin().getServer().getOnlinePlayers().size(), 6);

        definePlayer.addObjective("playersblank", ChatColor.GREEN + "", 5);

        definePlayer.addObjective("rubies", ChatColor.RED + "" + ChatColor.BOLD + "Rubies", 4);

        definePlayer.addObjective("rubiesvalue", ChatColor.RESET + "" + ChatColor.WHITE + "0", 3);

        definePlayer.addObjective("rubiesblank", ChatColor.RED + "", 2);

        definePlayer.addObjective("divider", ChatColor.WHITE + "" + ChatColor.BOLD + "-------------", 1);

        definePlayer.addObjective("servername", ChatColor.GREEN + "play.abyssmc.org", 0);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (definePlayer.getScoreboardName().equals("Game")) {
                        definePlayer.updateObjective("totalplayers", ChatColor.WHITE + "" + MainAPI.getPlugin().getServer().getOnlinePlayers().size());
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

    public Location closestAxisToLocation(Location firstLocation, List<Location> compareLocations) {
        Double closest = Double.MAX_VALUE;


        //Bukkit.broadcastMessage(firstLocation.toString() + " " + firstLocation.getX() + " " + firstLocation.getZ());

        if (compareLocations.size() > 0) {
            Location closestLocation = compareLocations.get(0);
            for (Location secondLocation : compareLocations) {
                Double farthestAxis;

                double X = Math.abs(firstLocation.getX() - secondLocation.getX());
                double Z = Math.abs(firstLocation.getZ() - secondLocation.getZ());

                //Bukkit.broadcastMessage(secondLocation.toString() + " " + X + " " + Z);

                if (X > Z) {
                    farthestAxis = X;
                } else {
                    farthestAxis = Z;
                }

                if (farthestAxis < closest) {
                    closestLocation = secondLocation;
                    closest = farthestAxis;
                }
            }

            return closestLocation;
        }

        new Exception().printStackTrace();
        return new Location(Bukkit.getWorld("world"), 0, 70, 0);
    }

    // All getters and setters, nothing to see here

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setGameCountdown(int gameCountdown) {
        this.gameCountdown = gameCountdown;
    }

    public boolean isAllowBlockBreak() {
        return allowBlockBreak;
    }

    public void setAllowBlockBreak(boolean allowBlockBreak) {
        this.allowBlockBreak = allowBlockBreak;
    }

    public boolean isAllowBlockForm() {
        return allowBlockForm;
    }

    public void setAllowBlockForm(boolean allowBlockForm) {
        this.allowBlockForm = allowBlockForm;
    }

    public boolean isAllowBlockMelt() {
        return allowBlockMelt;
    }

    public void setAllowBlockMelt(boolean allowBlockMelt) {
        this.allowBlockMelt = allowBlockMelt;
    }

    public boolean isAllowBlockPlace() {
        return allowBlockPlace;
    }

    public void setAllowBlockPlace(boolean allowBlockPlace) {
        this.allowBlockPlace = allowBlockPlace;
    }

    public boolean isAllowBlockSpread() {
        return allowBlockSpread;
    }

    public void setAllowBlockSpread(boolean allowBlockSpread) {
        this.allowBlockSpread = allowBlockSpread;
    }

    public void setAllowExplosions(boolean allowExplosions) {
        this.allowExplosions = allowExplosions;
    }

    public void setAllowFluidFlow(boolean allowFluidFlow) {
        this.allowFluidFlow = allowFluidFlow;
    }

    public boolean isAllowHunger() {
        return allowHunger;
    }

    public boolean isAllowItemDrop() {
        return allowItemDrop;
    }

    public boolean isAllowDurability() {
        return allowDurability;
    }

    public void setAllowLeafDecay(boolean allowLeafDecay) {
        this.allowLeafDecay = allowLeafDecay;
    }

    public boolean isAllowEnterBed() {
        return allowEnterBed;
    }

    public boolean isAllowWeatherChange() {
        return allowWeatherChange;
    }

    public SpawnReason[] getAllowedSpawnReasons() {
        return allowedSpawnReasons;
    }

    public boolean isVoidInstantKill() {
        return voidInstantKill;
    }

    public boolean isAllowPlayersToJoinNow() {
        return allowPlayersToJoinNow;
    }

    public void setAllowPlayersToJoinNow(boolean allowPlayersToJoinNow) {
        this.allowPlayersToJoinNow = allowPlayersToJoinNow;
    }

    public ArrayList<DefinePlayer> getUuidParticipating() {
        return uuidParticipating;
    }

    public boolean isAllowPlayerPortal() {
        return allowPlayerPortal;
    }

    public boolean isAlwaysAllowPlayersJoin() {
        return alwaysAllowPlayersJoin;
    }

    public void setAlwaysAllowPlayersJoin(boolean alwaysAllowPlayersJoin) {
        this.alwaysAllowPlayersJoin = alwaysAllowPlayersJoin;
    }

    public boolean isGameStarting() {
        return isGameStarting;
    }

    public UUID getGameUUID() {
        return gameUUID;
    }

    public File getWorldFolder() {
        return worldFolder;
    }

    public void setWorldFolder(File worldFolder) {
        this.worldFolder = worldFolder;
    }

    public DefineWorld getGameWorld() {
        return gameWorld;
    }

    public void setGameWorld(DefineWorld gameWorld) {
        this.gameWorld = gameWorld;
    }

    public boolean isCanGameStart() {
        return canGameStart;
    }

    public void setCanGameStart(boolean canGameStart) {
        this.canGameStart = canGameStart;
    }

    public boolean isAllowPlayerDamage() {
        return allowPlayerDamage;
    }

    public void setAllowPlayerDamage(boolean allowPlayerDamage) {
        this.allowPlayerDamage = allowPlayerDamage;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public void setSpawnItemList(ItemStack[] spawnItemList) {
        this.spawnItemList = spawnItemList;
    }

    public boolean isAllowPlayerShootProjectile() {
        return allowPlayerShootProjectile;
    }

    public void setAllowPlayerShootProjectile(boolean allowPlayerShootProjectile) {
        this.allowPlayerShootProjectile = allowPlayerShootProjectile;
    }

    public String getSpawnKitName() {
        return spawnKitName;
    }

    public void setSpawnKitName(String spawnKitName) {
        this.spawnKitName = spawnKitName;
    }

    public void setGameCountdownDecrement(int gameCountdownDecrement) {
        this.gameCountdownDecrement = gameCountdownDecrement;
    }

    public int getGameCountdownAlwaysAnnounce() {
        return gameCountdownAlwaysAnnounce;
    }

    public void setAllowPlayerMoveOnJoin(boolean allowPlayerMoveOnJoin) {

        this.allowPlayerMoveOnJoin = allowPlayerMoveOnJoin;
    }

    public int getGameEndCountdown() {
        return gameEndCountdown;
    }

    public boolean isAllowPlayerDoubleJump() {
        return allowPlayerDoubleJump;
    }

    public void setAllowPlayerDoubleJump(boolean allowPlayerDoubleJump) {
        this.allowPlayerDoubleJump = allowPlayerDoubleJump;
    }

    public Location getJoinServerLocation() {
        return joinServerLocation;
    }

    public void setJoinServerLocation(Location joinServerLocation) {
        this.joinServerLocation = joinServerLocation;
    }

    public boolean isAllowGoldenLaunchpads() {
        return allowGoldenLaunchpads;
    }

    public void setAllowGoldenLaunchpads(boolean allowGoldenLaunchpads) {
        this.allowGoldenLaunchpads = allowGoldenLaunchpads;
    }

    public void setGameUUID(UUID gameUUID) {
        this.gameUUID = gameUUID;
    }

    public void setGameStarting(boolean gameStarting) {
        isGameStarting = gameStarting;
    }

    public boolean getGameStarting() {
        return isGameStarting;
    }

    public boolean getCanGameStart() {
        return canGameStart;
    }

    public boolean isAllowCrafting() {
        return allowCrafting;
    }

    public void setAllowCrafting(boolean allowCrafting) {
        this.allowCrafting = allowCrafting;
    }

    public void setAllowItemDrop(boolean allowItemDrop) {
        this.allowItemDrop = allowItemDrop;
    }

    public int getTeams() {
        return teams;
    }

    public void setTeams(int teams) {
        this.teams = teams;
    }

    public boolean isGenerateTeams() {
        return generateTeams;
    }

    public void setGenerateTeams(boolean generateTeams) {
        this.generateTeams = generateTeams;
    }

    public ArrayList<DefineTeam> getUuidDefineTeams() {
        return uuidDefineTeams;
    }

    public void setUuidDefineTeams(ArrayList<DefineTeam> uuidDefineTeams) {
        this.uuidDefineTeams = uuidDefineTeams;
    }
}
