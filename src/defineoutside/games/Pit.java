package defineoutside.games;

import defineoutside.creator.DefinePlayer;
import defineoutside.creator.Game;
import defineoutside.main.PlayerManager;

import java.util.UUID;

public class Pit extends Game {
    @Override
    public void createGameWorldAndRegisterGame() {
        setGameType("pit");
        setMaxPlayers(32);
        setAlwaysAllowPlayersJoin(true);
        super.createGameWorldAndRegisterGame();
    }

    @Override
    public void playerLoad(UUID uuid) {
        super.playerLoad(uuid);

        PlayerManager pm = new PlayerManager();
        DefinePlayer definePlayer = pm.getDefinePlayer(uuid);

        definePlayer.setCanInfiniteRespawn(true);
    }

    // Only end without players
    @Override
    public boolean checkEndByEliminations() {
        if (getUuidParticipating().size() == 0) {
            end();
            return true;
        }
        return false;
    }
}
