package defineoutside.games;

import defineoutside.creator.DefinePlayer;
import defineoutside.creator.Game;
import defineoutside.main.PlayerManager;

import java.util.UUID;

public class Duel extends Game {
    @Override
    public void createGameWorldAndRegisterGame() {
        setGameType("duel");
        setAllowPlayerMoveOnJoin(false);
        setMaxPlayers(2);
        super.createGameWorldAndRegisterGame();
    }

    @Override
    public void playerLoad(UUID uuid) {
        super.playerLoad(uuid);

        PlayerManager pm = new PlayerManager();
        DefinePlayer definePlayer = pm.getDefinePlayer(uuid);
        definePlayer.setCanInfiniteRespawn(false);
    }
}
