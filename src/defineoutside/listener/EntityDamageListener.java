package defineoutside.listener;

import defineoutside.creator.DefinePlayer;
import defineoutside.main.GameManager;
import defineoutside.main.PlayerManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // Literally any damage
        if (event.getEntity() instanceof HumanEntity) {
            GameManager gm = new GameManager();
            event.setCancelled(!gm.getGameFromWorld(event.getEntity().getWorld()).isAllowPlayerDamage());
        }

        // Death
        if (event.getEntity() instanceof HumanEntity && !event.isCancelled() && ((Player) event.getEntity()).getHealth() - event.getFinalDamage() <= 0) {
            PlayerManager pm = new PlayerManager();
            GameManager gm = new GameManager();

            DefinePlayer dp = pm.getDefinePlayer((Player) event.getEntity());

            if (dp.playerDeathCanRespawn()) {
                gm.getGameFromWorld(event.getEntity().getWorld()).playerDeath(pm.getDefinePlayer((Player) event.getEntity()), true);
            } else {
                gm.getGameFromWorld(event.getEntity().getWorld()).playerDeath(pm.getDefinePlayer((Player) event.getEntity()), false);
            }
            event.setCancelled(true);
        }
    }
}