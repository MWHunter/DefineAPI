package defineoutside.main;

import defineoutside.creator.DefinePlayer;
import defineoutside.listener.InventoryClickListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ActionParser {
    public static boolean doAction(UUID playerUUID, String action, String subaction) {
        return doAction(Bukkit.getPlayer(playerUUID), action, subaction);
    }

    public static boolean doAction(Player player, String action, String subaction) {
        if (action.equalsIgnoreCase("Queue")) {
            Matchmaking mm = new Matchmaking();
            UUID playerUUID = player.getUniqueId();

            mm.addPlayerToCentralQueue(playerUUID, subaction);

            return true;
        }

        if (action.equalsIgnoreCase("Kit")) {
            PlayerManager pm = new PlayerManager();
            DefinePlayer dp = pm.getDefinePlayer(player);
            player.sendMessage(ChatColor.GRAY + "Game > " + ChatColor.WHITE + "You have selected the kit " + ChatColor.RED + subaction);
            dp.setKit(subaction);

            return true;
        }

        // Open an inventory
        if (action.equalsIgnoreCase("Inventory")) {
            Inventory myInventory = Bukkit.createInventory(null, 54, "My custom Inventory!");

            ItemStack[] stacks = ConfigManager.getKitGUI(subaction);
            for (int i = 0; i < stacks.length; i++) {
                myInventory.setItem(i, stacks[i]);
            }

            InventoryClickListener.openCustomInventory(player.getUniqueId());
            player.openInventory(myInventory);
        }

        return false;
    }
}
