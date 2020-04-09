package defineoutside.games;

import defineoutside.creator.DefinePlayer;
import defineoutside.creator.Game;

public class Duel extends Game {
    @Override
    public void createGameWorldAndRegisterGame() {
        setGameType("duel");
        setAllowPlayerMoveOnJoin(false);
        setMaxPlayers(2);
        super.createGameWorldAndRegisterGame();
    }

    @Override
    public void playerLoad(DefinePlayer definePlayer) {
        super.playerLoad(definePlayer);

        definePlayer.setCanInfiniteRespawn(false);
    }
}
