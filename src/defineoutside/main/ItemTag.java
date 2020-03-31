package defineoutside.main;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ItemTag {
    public static ItemStack setTag(ItemStack item, String tag, String value) {
        net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();

        compound.setString(tag, value);
        nmsStack.setTag(compound);

        item = CraftItemStack.asBukkitCopy(nmsStack);

        return item;
    }

    public static String getTag(ItemStack item, String tag) {
        net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();

        String returnString = compound.getString(tag);

        return returnString;
    }
}