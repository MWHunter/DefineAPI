package defineoutside.main;

import defineoutside.creator.DefinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ActionParser {
    public boolean doAction(UUID playerUUID, String action, String subaction) {
        return doAction(Bukkit.getPlayer(playerUUID), action, subaction);
    }

    public boolean doAction(Player player, String action, String subaction) {
        if (action.equalsIgnoreCase("Queue")) {
            Matchmaking mm = new Matchmaking();
            UUID playerUUID = player.getUniqueId();

            //String existingQueueType = mm.inQueueForGame(playerUUID);
            //if (existingQueueType != null && existingQueueType.equalsIgnoreCase(subaction)) { // Already in this game, means the player wants to cancel queue?
            //    mm.removePlayer(playerUUID);
            //    player.sendMessage(ChatColor.GRAY + "Queue > " + ChatColor.WHITE + "You are no longer in queue for game type " + ChatColor.RED + subaction);
            //} else { // Not in this game mode type
                mm.addPlayerToCentralQueue(playerUUID, subaction);
                //player.getPlayer().sendMessage(ChatColor.GRAY + "Queue > " + ChatColor.WHITE + "You are in queue for game type " + ChatColor.RED + subaction);
            //}

            return true;
        }

        if (action.equalsIgnoreCase("Kit")) {
            PlayerManager pm = new PlayerManager();
            DefinePlayer dp = pm.getDefinePlayer(player);
            player.sendMessage(ChatColor.GRAY + "Game > " + ChatColor.WHITE + "You have selected the kit " + ChatColor.RED + subaction);
            dp.setKit(subaction);

            return true;
        }
        return false;
    }
}
