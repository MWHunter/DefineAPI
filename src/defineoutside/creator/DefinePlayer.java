package defineoutside.creator;

import defineoutside.main.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import java.util.UUID;

public class DefinePlayer {

    private DefineTeam playerDefineTeam;
    private String inGameType = "lobby";
    private String kit = "empty";

    private int lives = 0;

    private boolean canInfiniteRespawn = true;

    private boolean lockInGame = false;
    private boolean isFrozen = false;

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    private boolean isAlive = true;

    UUID playerUUID = null;

    public boolean isFrozen() {
        return isFrozen;
    }

    // TODO: Prevent damage while frozen
    public void setFreeze(Boolean freeze) {
        if (freeze) {
            isFrozen = true;
            // Prevent players from moving
            Entity pig = getBukkitPlayer().getLocation().getWorld().spawnEntity(getBukkitPlayer().getLocation(), EntityType.PIG);
            ((LivingEntity) pig).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1, false, false));
            pig.addPassenger(getBukkitPlayer());
            pig.setCustomName("player freeze");
            pig.setCustomNameVisible(false);
            pig.setSilent(true);
            pig.setInvulnerable(true);
            ((LivingEntity) pig).setAI(false);
        } else {
            if (isFrozen) {
                isFrozen = false;

                //getBukkitPlayer().eject();
                //getBukkitPlayer().getVehicle().eject();
                getBukkitPlayer().getVehicle().setInvulnerable(false);
                getBukkitPlayer().getVehicle().remove();
            }
        }
    }

    public void createPlayer(UUID uuid) {
        this.playerUUID = uuid;
        this.playerDefineTeam = new DefineTeam();
        PlayerManager pm = new PlayerManager();
        pm.registerPlayer(this);
    }

    public void removePlayer() {
        PlayerManager pm = new PlayerManager();
        pm.removePlayer(this);
    }

    public void reset() {
        lives = 0;
        canInfiniteRespawn = true;
        isAlive = true;
    }

    public boolean playerDeathCanRespawn() {
        if (canInfiniteRespawn) {
            return true;
        } else if (lives > 0) {
            lives--;
            return true;
        }

        isAlive = false;
        return false;
    }

    public static void setScoreBoard(Scoreboard scoreBoard) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("ServerName", "dummy", "Test Server");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score onlineName = obj.getScore(ChatColor.GRAY + "» Online");
        onlineName.setScore(15);

        Team onlineCounter = board.registerNewTeam("onlineCounter");
        onlineCounter.addEntry(ChatColor.BLACK + "" + ChatColor.WHITE);

        if (Bukkit.getOnlinePlayers().size() == 0) {
            onlineCounter.setPrefix(ChatColor.DARK_RED + "0" + ChatColor.RED + "/" + ChatColor.DARK_RED + Bukkit.getMaxPlayers());
        } else {
            onlineCounter.setPrefix("" + ChatColor.DARK_RED + Bukkit.getOnlinePlayers().size() + ChatColor.RED + "/" + ChatColor.DARK_RED + Bukkit.getMaxPlayers());
        }

        obj.getScore(ChatColor.BLACK + "" + ChatColor.WHITE).setScore(14);
    }

    // All getters and setters below here
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    // Assume the player is alone without a team
    public DefineTeam getPlayerDefineTeam() {
        return playerDefineTeam;
    }

    public void setPlayerDefineTeam(DefineTeam playerDefineTeam) {
        playerDefineTeam.addPlayer(playerUUID);
        this.playerDefineTeam = playerDefineTeam;
    }


    public int getLives() {
        return lives;
    }


    public void setLives(int lives) {
        this.lives = lives;
    }


    public boolean getCanInfiniteRespawn() {
        return canInfiniteRespawn;
    }


    public void setCanInfiniteRespawn(boolean canRespawn) {
        this.canInfiniteRespawn = canRespawn;
    }

    public String getInGameType() {
        return inGameType;
    }

    public void setInGameType(String inGameType) {
        this.inGameType = inGameType;
    }

    public boolean isLockInGame() {
        return lockInGame;
    }

    public void setLockInGame(boolean lockInGame) {
        this.lockInGame = lockInGame;
    }

    public String getKit() {
        return kit;
    }

    public void setKit(String kit) {
        this.kit = kit;
    }
}
