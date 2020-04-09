package defineoutside.games;

import defineoutside.creator.DefinePlayer;
import defineoutside.creator.Game;

public class Pit extends Game {
    @Override
    public void createGameWorldAndRegisterGame() {
        setGameType("pit");
        setMaxPlayers(32);
        setAlwaysAllowPlayersJoin(true);
        super.createGameWorldAndRegisterGame();
    }

    @Override
    public void playerLoad(DefinePlayer definePlayer) {
        super.playerLoad(definePlayer);

        definePlayer.setCanInfiniteRespawn(true);
    }

    // Only end without players
    @Override
    public void checkEndByEliminations() {
        if (getUuidParticipating().size() == 0) {
            end();
        }
    }
}
