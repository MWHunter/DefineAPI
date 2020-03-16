package defineoutside.games;

import defineoutside.creator.DefineWorld;
import defineoutside.creator.Game;
import defineoutside.main.GameManager;

public class DisabledDimension extends Game {
    @Override
    public void createGameWorldAndRegisterGame() {
        setAllowPlayersToJoinNow(false);
        setAlwaysAllowPlayersJoin(false);

        // Prepare world
        DefineWorld dwNether = new DefineWorld();
        GameManager gm = new GameManager();
        // MainAPI abuses game type to store world name
        dwNether.createArena(this.getGameType(), null);
        // Next, just set it back
        setGameType("Disabled Dimension");

        gm.registerWorld(getGameUUID(), dwNether);

        super.createGameWorldAndRegisterGame();
    }

    @Override
    public void startGameCountdown() {
        //Bukkit.broadcastMessage(ChatColor.RED + "You have discovered an easter egg, getting the server to load a game in an unloaded dimension!");
    }
}
