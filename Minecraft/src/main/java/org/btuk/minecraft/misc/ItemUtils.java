package org.btuk.minecraft.misc;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemUtils {

    private ItemUtils() {

    }

    public static ItemStack createItem(Material material, int amount, Component displayName, Component... loreString) {
        ItemStack item = ItemStack.of(material.isItem() ? material : Material.STRUCTURE_VOID);
        item.setAmount(amount);

        ItemMeta meta = item.getItemMeta();
        meta.displayName(displayName);
        List<Component> lore = new ArrayList<>(Arrays.asList(loreString));
        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Set the item in the selected slot of the player's inventory. If the player is holding an item, it will be replaced.
     * If the player already has the item in their inventory, it will be moved to the selected slot.
     *
     * @param player the player to give the item to
     * @param item   the item to give to the player
     */
    public static void setItemInSelectedSlot(Player player, ItemStack item) {

        int selectedSlot = player.getInventory().getHeldItemSlot();

        for (ItemStack inventoryItem : player.getInventory()) {
            if (item.equals(inventoryItem)) {
                player.getInventory().remove(inventoryItem);
                return;
            }
        }

        player.getInventory().setItem(selectedSlot, item);
    }
}
