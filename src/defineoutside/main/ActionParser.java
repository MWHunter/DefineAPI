package defineoutside.main;

import defineoutside.creator.DefinePlayer;
import defineoutside.listener.InventoryClickListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ActionParser {
    public static boolean doAction(DefinePlayer player, String action, String subaction) {
        if (action.equalsIgnoreCase("Queue")) {
            Matchmaking mm = new Matchmaking();

            mm.addPlayerToCentralQueue(player, subaction);

            return true;
        }

        if (action.equalsIgnoreCase("Kit")) {
            PlayerManager pm = new PlayerManager();

            player.getBukkitPlayer().sendMessage(ChatColor.GRAY + "Game > " + ChatColor.WHITE + "You have selected the kit " + ChatColor.RED + subaction);
            player.setKit(subaction);

            return true;
        }

        // Open an inventory
        if (action.equalsIgnoreCase("Inventory")) {
            Inventory myInventory = Bukkit.createInventory(null, 54, "Shop");

            ItemStack[] stacks = ConfigManager.getKitGUI(subaction);
            for (int i = 0; i < stacks.length; i++) {
                myInventory.setItem(i, stacks[i]);
            }

            InventoryClickListener.openCustomInventory(player.getPlayerUUID());
            player.getBukkitPlayer().openInventory(myInventory);
        }

        return false;
    }
}
