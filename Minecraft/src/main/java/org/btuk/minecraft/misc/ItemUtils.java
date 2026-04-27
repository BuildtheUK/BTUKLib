package org.btuk.minecraft.misc;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
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
}
