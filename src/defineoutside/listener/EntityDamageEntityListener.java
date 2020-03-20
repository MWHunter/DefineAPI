package defineoutside.listener;

import defineoutside.creator.DefineTeam;
import defineoutside.main.GameManager;
import defineoutside.main.PlayerManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageEntityListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamagePlayerEvent(EntityDamageByEntityEvent event) {
        // Check if the players are on the same team
        PlayerManager pm = new PlayerManager();

        if (event.getDamager() instanceof HumanEntity && event.getEntity() instanceof HumanEntity) {
            DefineTeam attackerDefineTeam = pm.getDefinePlayer((Player)event.getDamager()).getPlayerDefineTeam();
            DefineTeam victimDefineTeam = pm.getDefinePlayer((Player)event.getEntity()).getPlayerDefineTeam();

            if (attackerDefineTeam.equals(victimDefineTeam)) {
                event.setCancelled(!pm.getDefinePlayer((Player) event.getDamager()).getPlayerDefineTeam().allowTeamDamage);
            }

            else if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {

                // Check if the player died
                if (((Player) event.getEntity()).getHealth() - event.getFinalDamage() <= 0) {
                    GameManager gm = new GameManager();
                    gm.getGameFromWorld(event.getEntity().getWorld()).sendKillMessage((Player) event.getEntity(), (Player) event.getDamager());
                }
            }
        }
    }
}
