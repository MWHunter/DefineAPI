package defineoutside.listener;

import defineoutside.creator.DefinePlayer;
import defineoutside.main.ItemTag;
import defineoutside.main.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.UUID;

public class InventoryClickListener implements Listener {
    static HashSet<UUID> playerInCustomInventory = new HashSet<>();

    @EventHandler
    public void onClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (playerInCustomInventory.contains(player.getUniqueId())) {
            String action = ItemTag.getTag(clickedItem, "DefineShop");
            if (action.equalsIgnoreCase("Buy")) {
                String boughtItem = ItemTag.getTag(clickedItem, "BuyItem").toUpperCase();
                String boughtItemAmount = ItemTag.getTag(clickedItem, "BuyItemAmount");
                String cost = ItemTag.getTag(clickedItem, "Cost");

                DefinePlayer definePlayer = PlayerManager.getDefinePlayer(event.getWhoClicked().getUniqueId());

                // -1 means the inventory is full
                if (player.getInventory().firstEmpty() != -1) {
                    if (definePlayer.getMoney() > Integer.parseInt(cost)) {
                        definePlayer.setMoney(definePlayer.getMoney() - Integer.parseInt(cost));
                        player.getInventory().addItem(new ItemStack(Material.valueOf(boughtItem), Integer.parseInt(boughtItemAmount)));

                        player.sendMessage(ChatColor.WHITE + "You bought " + boughtItemAmount + " " + boughtItem.toLowerCase() + " for " + ChatColor.GOLD + cost + " gold");
                    } else {
                        // No gold
                        player.sendMessage(ChatColor.RED + "You do not have enough gold to purchase this item");
                    }
                }  else {
                    // No inventory space
                    player.sendMessage(ChatColor.RED + "Your inventory is full");
                }
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        closeCustomInventory(event.getPlayer().getUniqueId());
    }

    public static void openCustomInventory(UUID uuid) {
        playerInCustomInventory.add(uuid);
    }

    public static void closeCustomInventory(UUID uuid) {
        playerInCustomInventory.remove(uuid);
    }
}
