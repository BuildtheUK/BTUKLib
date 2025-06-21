package net.bteuk.minecraft.misc;

import net.bteuk.minecraft.component.ComponentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Set;

/**
 * A collection of static utils related to the Minecraft Player
 */
public class PlayerUtils {

    /** A list of items which cannot be overridden when given a player an item */
    public static Set<ItemStack> protectedItems;

    /**
     * Gives a player an item, it will be set in their main hand, if it does not already exist there.
     * <p>
     * If the main hand is empty, set it there.
     * If no empty slots are available set it to the hand slot.
     * If the hand slot contains a protected item, identify the first slot which does not.
     * @param p The player to give the item to
     * @param item The item to give to the player
     * @param name The human friendly name of the item
     */
    public static void giveItem(Player p, ItemStack item, String name) {

        // If we already have the item switch to current slot.
        if (p.getInventory().containsAtLeast(item, 1)) {

            // Swap the position of the target item with the current item in
            p.getInventory().setItem(p.getInventory().first(item), p.getInventory().getItemInMainHand());
            p.getInventory().setItemInMainHand(item);
            p.sendMessage(ComponentUtils.success("Set ").append(Component.text(name, NamedTextColor.DARK_AQUA)
                    .append(ComponentUtils.success(" to main hand."))));
        }

        else {
            // Identifies an empty slot
            int emptySlot = getAvailableHotbarSlot(p, protectedItems);

            if (emptySlot >= 0) {
                // Set item to empty slot.
                p.getInventory().setItem(emptySlot, item);
                p.sendMessage(ComponentUtils.success("Set ").append(Component.text(name, NamedTextColor.DARK_AQUA)
                        .append(ComponentUtils.success(" to slot " + (emptySlot + 1)))));
            } else {
                // All slots are filled with protected items - this would be a very rare scenario
                p.sendMessage(ComponentUtils.error("There are no available slots in your hotbar"));
            }
        }
    }

    /**
     * Cycles through the hot bar from 1 to 9, until an available spot is found
     * @param p The player to return an empty hotbar slot for
     * @param protectedItems A collection of items which cannot be replaced
     * @return an empty hotbar slot, if no empty slot exists return -1.
     */
    public static int getAvailableHotbarSlot(Player p, Collection<ItemStack> protectedItems) {

        // If main hand is empty return that slot.
        ItemStack heldItem = p.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            return p.getInventory().getHeldItemSlot();
        }

        //Stores a record of which slots hold protected items
        short protectedSlots = 0b000000000;

        // Check if hotbar has an empty slot.
        for (int i = 0; i < 9; i++) {

            ItemStack item = p.getInventory().getItem(i);

            if (item == null) {
                return i;
            }
            if (item.getType() == Material.AIR){
                return i;
            }
            //Check for protected items
            for (ItemStack protectedItem : protectedItems) {
                if (protectedItem.equals(item))
                {
                    //This slot contains protected item, so record
                    protectedSlots = (short) (protectedSlots | (1<<i));
                }
            }
        }

        // If no empty slot was found, try again, but allow overriding on items which are not protected
        // Check the current slot first
        int iCurrentSlot = p.getInventory().getHeldItemSlot();
        if ((protectedSlots & 1<<iCurrentSlot) == 0b000000000)
            return iCurrentSlot;

        // If the hand slot was protected, cycle through the binary, identifying the first overridable slot
        for (int iSlot = 0 ; iSlot < 9 ; iSlot++) {

            if ((protectedSlots & 1<<iSlot) == 0b000000000)
                return iSlot;
        }

        // No slot could be found, return -1.
        return -1;
    }
}
