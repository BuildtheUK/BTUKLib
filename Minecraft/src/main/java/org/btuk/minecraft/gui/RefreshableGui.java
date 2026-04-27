package org.btuk.minecraft.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class RefreshableGui extends Gui {

    public RefreshableGui(GuiManager guiManager, int inventorySize, Component inventoryName) {
        super(guiManager, inventorySize, inventoryName);
    }

    public RefreshableGui(GuiManager guiManager, Inventory inventory) {
        super(guiManager, inventory);
    }

    protected abstract void createGui();

    /**
     * Ensures the gui is refreshed on opening.
     *
     * @param player the player to open the gui for
     */
    @Override
    public void open(Player player) {
        this.refresh();
        super.open(player);
    }

    /**
     * Refresh the gui by clearing it and creating it again.
     */
    public void refresh() {
        clear();
        createGui();
    }
}
