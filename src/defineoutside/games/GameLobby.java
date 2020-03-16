package defineoutside.games;

import defineoutside.creator.Game;
import defineoutside.main.GameManager;
import defineoutside.main.MainAPI;
import org.bukkit.scheduler.BukkitRunnable;

public class GameLobby extends Game {
    private String lobbyForGametype = "any";

    @Override
    public void createGameWorldAndRegisterGame() {
        if (getLobbyForGametype() == null) {
            lobbyForGametype = "any";
        }
        setGameType("gamelobby: " + getLobbyForGametype());
        setGameCountdown(5);
        setGameCountdownDecrement(10);

        setAllowPlayerMoveOnJoin(true);
        setAllowPlayersToJoinNow(true);
        setAllowPlayerDamage(false);
        setAllowPlayerShootProjectile(false);
        super.createGameWorldAndRegisterGame();
    }

    @Override
    public void start() {
        GameManager gm = new GameManager();


        Game game;

        game = gm.createLocalGame(getLobbyForGametype());

        gm.transferPlayers(getUuidParticipating(), game);
    }

    @Override
    public void doEndCountdown() {
        if (!isGameEnding) {
            isGameEnding = true;

            // No players, or everyone is one one team.  End the game after the countdown
            new BukkitRunnable() {
                public void run() {
                    end();
                }
            }.runTaskLater(MainAPI.getPlugin(), getGameEndCountdown() * 20L);
        }
    }

    public String getLobbyForGametype() {
        return lobbyForGametype;
    }

    public void setLobbyForGametype(String lobbyForGametype) {
        GameManager gm = new GameManager();
        setMinPlayers(gm.getMinPlayers(lobbyForGametype));
        this.lobbyForGametype = lobbyForGametype;
    }
}
