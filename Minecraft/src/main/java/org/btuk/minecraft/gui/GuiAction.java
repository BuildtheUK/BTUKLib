package org.btuk.minecraft.gui;

import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface GuiAction {

    void click(InventoryClickEvent clickEvent);
}
