package defineoutside.main;

import defineoutside.creator.DefinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {
    private static HashMap<UUID, DefinePlayer> completePlayerMap = new HashMap<UUID, DefinePlayer>();

    public void registerPlayer(DefinePlayer definePlayer) {
        completePlayerMap.put(definePlayer.getPlayerUUID(), definePlayer);
    }

    public void removePlayer(DefinePlayer definePlayer) {
        completePlayerMap.remove(definePlayer.getPlayerUUID());
    }

    public DefinePlayer getDefinePlayer(Player player) {
        return completePlayerMap.get(player.getUniqueId());
    }

    public DefinePlayer getDefinePlayer(UUID playerUUID) {
        return completePlayerMap.get(playerUUID);
    }

    public HashMap<UUID, DefinePlayer> getAllPlayers() {
        return completePlayerMap;
    }
}
