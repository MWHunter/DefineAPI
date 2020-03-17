package defineoutside.creator;

import defineoutside.main.*;
import io.papermc.lib.PaperLib;
import org.bukkit.*;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class Game {
    // Static abuse here due to inheriting classes having issues with variables changing on them
    private int minPlayers = 2;
    private int maxPlayers = 999;
    private int teams = 2;
    private int gameCountdown = 10;
    private int gameCountdownDecrement = 5;
    private int gameCountdownAlwaysAnnounce = 4;
    private int gameEndCountdown = 5;
    private int maxGameTime = 30 * 60;
    private int respawnTime = 5;
    private int teleportIndex = 0;

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

    private boolean allowPlayerMoveOnJoin = true;
    public boolean allowPlayerDoubleJump = false;

    private boolean voidInstantKill = true;

    private boolean allowPlayersToJoinNow = true;
    private boolean alwaysAllowPlayersJoin = false;

    private boolean isGameStarting = false;
    private boolean canGameStart = true;
    public boolean isGameEnding = false;
    private boolean hasStarted = false;

    // These two should be set by gamemanager
    private File worldFolder = null;

    private String gameType = "testwars";
    private String spawnKitName = "empty";

    //private String spawnKit = "default";

    SpawnReason[] allowedSpawnReasons = new SpawnReason[]{SpawnReason.CUSTOM, SpawnReason.SHEARED, SpawnReason.SHOULDER_ENTITY,
            SpawnReason.SLIME_SPLIT, SpawnReason.SILVERFISH_BLOCK, SpawnReason.SPAWNER_EGG};
    String[] killMessageStrings = new String[]{"You rekt ", "You destroyed ", "You OwO'd ", "You stabbed ", "You misclicked on "};

    // 16 ready teams in total
    String[] teamNames = new String[]{"Red", "Blue", "Lime", "Yellow", "White", "Orange", "Magenta", "Light Blue", "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Green", "Black", "Brown"};
    Material[] teamWools = new Material[]{Material.RED_WOOL, Material.BLUE_WOOL, Material.LIME_WOOL, Material.YELLOW_WOOL, Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL,
            Material.LIGHT_BLUE_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.GREEN_WOOL, Material.BLACK_WOOL, Material.BROWN_WOOL};
    ChatColor[] teamColors = new ChatColor[]{ChatColor.RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.WHITE, ChatColor.GOLD, ChatColor.DARK_PURPLE, ChatColor.AQUA, ChatColor.LIGHT_PURPLE,
            ChatColor.DARK_GRAY, ChatColor.GRAY, ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.GREEN, ChatColor.BLACK, ChatColor.WHITE};

    public ArrayList<UUID> uuidParticipating = new ArrayList<UUID>();
    public ArrayList<UUID> uuidSpectating = new ArrayList<UUID>();

    public ArrayList<Team> uuidTeams = new ArrayList<>();

    public ItemStack[] spawnItemList = new ItemStack[41];

    private UUID gameUUID;

    DefineWorld gameWorld = new DefineWorld();

    Random random = new Random();

    public Location joinServerLocation = new Location(Bukkit.getWorld("world"), 0, 70, 0);

    public void createGameWorldAndRegisterGame() {

        Bukkit.getLogger().log(Level.SEVERE, gameUUID + " is UUID");
        if (gameUUID == null) {
            gameUUID = UUID.randomUUID();
            Bukkit.getLogger().log(Level.SEVERE, gameUUID + " is chosen");
        }

        for (int i = 0; i < teams; i++) {
            Team team = new Team();
            for (String teamName : teamNames) {
                boolean isUniqueName = true;

                for (Team teams : uuidTeams) {
                    if (teams.getName().equals(teamName)) {
                        isUniqueName = false;
                    }
                }

                if (isUniqueName) {
                    team.setName(teamName);

                    if (team != null) {
                        team.setWoolType(teamWools[ArrayUtils.indexOf(teamNames, teamName)]);
                        team.setChatColor(teamColors[ArrayUtils.indexOf(teamNames, teamName)]);
                    }

                    break;
                }
            }

            uuidTeams.add(team);
        }

        ConfigManager cm = new ConfigManager();

        // TODO: Attempt to get world spawns
        if (getWorldFolder() == null) {
            setWorldFolder(new File("world"));
        }

        List<List<Location>> listOfListOfSpawns = cm.getListOfSpawns(getWorldFolder().getName());

        for (int i = 0; i < teams; i++) {
            Team itTeam = uuidTeams.get(i);

            itTeam.setSpawns(listOfListOfSpawns.get(i % listOfListOfSpawns.size()));
        }

        spawnItemList = cm.getRandomKit(gameType);
        GameManager gm = new GameManager();
        gm.registerGame(gameUUID, this);
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

        for (UUID uuid : getUuidParticipating()) {
            PlayerManager pm = new PlayerManager();
            DefinePlayer dp = pm.getDefinePlayer(uuid);
            ConfigManager cm = new ConfigManager();
            Player player = Bukkit.getPlayer(uuid);
            ItemTag it = new ItemTag();

            player.sendMessage(ChatColor.GRAY + "Game > " + ChatColor.WHITE + "This game has begun. " + ChatColor.RED + "GLHF!");

            playerLoad(uuid);
            dp.setFreeze(false);

            ItemStack[] array = cm.getKit(dp.getKit()).clone();

            for (int i = 0; i < array.length; i++) {
                int randomIndexToSwap = random.nextInt(array.length);

                ItemStack temp = array[randomIndexToSwap];
                array[randomIndexToSwap] = array[i];
                array[i] = temp;
            }

            for (ItemStack items : array) {
                if (!it.getTag(items, "Kit").equals("")) {
                    dp.setKit(it.getTag(items, "Kit"));
                    break;
                }
            }

            givePlayerKit(player, cm.getKit(dp.getKit()));
        }
    }

    public boolean checkEndByEliminations() {

        PlayerManager pm = new PlayerManager();

        if (isGameEnding == true) {
            return false;
        }

        // avoid null pointers
        if (getUuidParticipating().size() > 0) {
            Team playerTeam = null;

            // Logic for seeing if there are still multiple teams
            for (UUID uuid : getUuidParticipating()) {
                DefinePlayer dp = pm.getDefinePlayer(uuid);
                if (dp.isAlive() || dp.playerDeathCanRespawn()) {
                    if (playerTeam == null) {
                        playerTeam = dp.getPlayerTeam();
                    } else if (!pm.getDefinePlayer(uuid).getPlayerTeam().equals(playerTeam)) {
                        return false;
                    }
                }
            }
        }

        doEndCountdown();
        return true;
    }

    public void doEndCountdown() {
        if (!isGameEnding) {
            // TODO: Make a player messaging method
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
        GameManager gm = new GameManager();
        Matchmaking mm = new Matchmaking();

        // Garbage collection takes care of this current object once this is called
        // RIP game object :(
        gm.deleteWorld(gameUUID);
        gm.deleteGame(gameUUID);

        // This needs to be called after removing the game from the list, otherwise the player will just join back into this game
        // This should prioritize games over lobbies
        for (UUID playerUUID : this.getUuidParticipating()) {
            mm.addPlayer(playerUUID, "lobby");

            // TODO: Use configs to get this location, make sure to get the location out of the loop for efficiency
            // Async because players won't notice an extra second being spent for this
            PaperLib.teleportAsync(Bukkit.getPlayer(playerUUID), new Location(Bukkit.getWorld("world"), 0, 70, 0));
        }
        // Everyone is out, so it is now safe to unload the world
        // TODO: To save or not to save?

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

    public void playerLoad(UUID uuid) {
        PlayerManager pm = new PlayerManager();

        DefinePlayer definePlayer = pm.getDefinePlayer(uuid);

        // This should be changed when this class becomes generic
        definePlayer.reset();
        definePlayer.setCanInfiniteRespawn(false);
        definePlayer.setInGameType(gameType);


        playerRespawn(uuid, allowPlayerMoveOnJoin, !hasStarted);
    }

    public void playerRespawn(UUID uuid) {
        playerRespawn(uuid, true);
    }

    public void playerRespawn(UUID uuid, boolean canMove) {
        playerRespawn(uuid, canMove, false);
    }

    public void playerRespawn(UUID uuid, boolean canMove, boolean kitSelector) {
        PlayerManager pm = new PlayerManager();
        ConfigManager cm = new ConfigManager();
        Player bukkitPlayer = Bukkit.getPlayer(uuid);
        DefinePlayer definePlayer = pm.getDefinePlayer(uuid);

        // This will null pointer if the player leaves the same tick as the game starts
        try {
            bukkitPlayer.setGameMode(GameMode.SURVIVAL);
            bukkitPlayer.setHealth(20);
            bukkitPlayer.getActivePotionEffects().clear();

            bukkitPlayer.setAllowFlight(allowPlayerDoubleJump);
        } catch (NullPointerException e) {
            MainAPI.getPlugin().getLogger().log(Level.WARNING, "Player left the same tick as the game started or as the player respawned!  Ignoring the respawn!");
        }

        ItemStack[] giveItems;
        if (kitSelector) {
            // TODO: Make this by team
            giveItems = cm.getRandomKitSelector(getGameType(), getWorldFolder().getName(), ArrayUtils.indexOf(teamNames, definePlayer.getPlayerTeam().getName()));
        } else {
            giveItems = cm.getKit(definePlayer.getKit());
        }

        givePlayerKit(bukkitPlayer, giveItems);

        // Check if the player has already been teleported to a valid spawn position
        //if (!definePlayer.getPlayerTeam().containsSpawn((int) bukkitPlayer.getLocation().getX(), (int) bukkitPlayer.getLocation().getZ()) &&
        //        !bukkitPlayer.getGameMode().equals(GameMode.SPECTATOR) && !bukkitPlayer.getLocation().getWorld().getName().equals(getGameWorld().getBukkitWorld().getName())) {
        Location teleportLocation = definePlayer.getPlayerTeam().getNextSpawn();
        // Config is loaded before we have the world ready, so we must set world
        teleportLocation.setWorld(getGameWorld().getBukkitWorld());

        PaperLib.teleportAsync(bukkitPlayer, teleportLocation).thenAccept(result -> {
            if (result) {
                if (canMove == false) {
                    definePlayer.setFreeze(true);
                }
            } else {
                bukkitPlayer.sendMessage(ChatColor.RED + "Something went wrong while teleporting you to the game.  Recovering by sending you back to the hub");
                Matchmaking mm = new Matchmaking();
                mm.addPlayerToCentralQueue(uuid, "lobby");
            }
        });
        //}
    }

    public void givePlayerKit(Player player, ItemStack[] giveItems) {
        player.getInventory().clear();

        for (int i = 0; i < 41; i++) {
            player.getInventory().setItem(i, giveItems[i]);
        }
    }

    public void playerJoin(Player player) {
        playerJoin(player.getUniqueId());
    }

    // TODO: Add a check if we still have enough players
    public void playerLeave(Player player) {
        playerLeave(player.getUniqueId());
    }

    public void playerJoin(UUID player) {
        PlayerManager pm = new PlayerManager();

        uuidParticipating.add(player);

        String joinMessage = ChatColor.GRAY + "Game > " + ChatColor.RED + Bukkit.getPlayer(player).getName() + ChatColor.WHITE + " has joined the game";

        DefinePlayer dp = pm.getDefinePlayer(player);
        dp.setPlayerTeam(getBestTeam());
        dp.setKit(spawnKitName);

        Team joinerTeam = pm.getDefinePlayer(player).getPlayerTeam();

        if (pm.getDefinePlayer(player).getPlayerTeam() != null) {
            joinMessage = ChatColor.GRAY + "Game > " + ChatColor.RED + Bukkit.getPlayer(player).getName() + ChatColor.WHITE + " has joined the " + joinerTeam.getChatColor() + joinerTeam.getName() + ChatColor.WHITE
                    + " team";
        }

        messageGamePlayers(joinMessage);

        playerLoad(player);

        attemptStart();
    }

    public void playerLeave(UUID player) {
        uuidParticipating.remove(player);

        for (UUID playerUUID : getUuidParticipating()) {
            Bukkit.getPlayer(playerUUID).sendMessage(ChatColor.GRAY + "Game > " + ChatColor.RED + Bukkit.getPlayer(player).getName() + ChatColor.WHITE + " has left the game");
        }

        PlayerManager pm = new PlayerManager();
        pm.getDefinePlayer(player).getPlayerTeam().removePlayer(player);

        checkEndByEliminations();
    }

    public void attemptStart() {
        Bukkit.broadcastMessage(uuidParticipating.size() + " is current playercount min is " + minPlayers + " " + !isGameStarting + " " + canGameStart);
        if (uuidParticipating.size() >= minPlayers && !isGameStarting && canGameStart) {
            startGameCountdown();
        }
    }

    public void sendKillMessage(Player playerKilled, Player killer) {
        playerKilled.sendMessage(ChatColor.GRAY + "Death > " + ChatColor.WHITE + "You were killed by " + ChatColor.RED + killer.getName() + ChatColor.WHITE +
                " with " + ChatColor.RED + (int) Math.round(killer.getHealth() * 10d) / 20d + "♥" + ChatColor.WHITE + " remaining");
        killer.sendMessage(ChatColor.GRAY + "Death > " + ChatColor.WHITE + killMessageStrings[random.nextInt(killMessageStrings.length)] + ChatColor.RED + playerKilled.getName());
    }

    public void playerDeath(DefinePlayer definePlayer, boolean canRespawn) {
        Player player = definePlayer.getBukkitPlayer();

        player.setGameMode(GameMode.SPECTATOR);
        if (canRespawn) {

            player.sendMessage(ChatColor.AQUA + "You have died!  Respawning in " + ChatColor.WHITE + (respawnTime - 1) + ChatColor.AQUA + " seconds.");
            new BukkitRunnable() {
                int currentSpawnTimer = respawnTime - 1;

                public void run() {

                    player.sendMessage(ChatColor.AQUA + "Respawning in " + ChatColor.WHITE + --currentSpawnTimer + ChatColor.AQUA + " seconds.");
                    if (currentSpawnTimer <= 0) {
                        playerRespawn(definePlayer.getPlayerUUID());
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

    public Team getBestTeam() {
        int smallestTeam = Integer.MAX_VALUE;
        // As a backup, shouldn't be needed
        Team bestTeam = new Team();

        for (Team team : uuidTeams) {
            if (team.getSizeOfTeam() < smallestTeam) {
                smallestTeam = team.getSizeOfTeam();
                bestTeam = team;
            }
        }

        return bestTeam;
    }

    public void messageGamePlayers(String string) {
        for (UUID uuid : getUuidParticipating()) {
            Bukkit.getPlayer(uuid).sendMessage(string);
        }
    }

    // All getters and setters, nothing to see here
    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

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

    public ArrayList<UUID> getUuidParticipating() {
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
}
