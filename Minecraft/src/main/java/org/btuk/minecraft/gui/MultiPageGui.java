package org.btuk.minecraft.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import org.btuk.minecraft.misc.ComponentUtils;
import org.btuk.minecraft.misc.ItemUtils;

/**
 * Generic implementation of a multi-page gui.
 */
public abstract class MultiPageGui extends RefreshableGui {

    private final int buttonCount;

    private final int buttonsPerPage;

    private final int inventorySize;

    private int page = 1;

    public MultiPageGui(GuiManager guiManager, int size, Component title, int buttonCount) {
        super(guiManager, size, title);
        this.inventorySize = size;
        this.buttonCount = buttonCount;

        this.buttonsPerPage = ((size / 9) - 2) * 7;
    }

    protected abstract void createPageButton(int slot, int index);

    protected abstract void addAdditionalButtons();

    protected void createGui() {

        // Make a button for each plot.
        for (int slot = 10, index = buttonsPerPage * (page - 1);
             index < (buttonsPerPage * page) && index < buttonCount;
             index++, slot += (slot % 9 == 7) ? 3 : 1
        ) {
            createPageButton(slot, index);
        }

        // If page is greater than 1 add a previous page button.
        int row = ((inventorySize / 9) + 1) / 2;
        if (page > 1) {
            setItem((row - 1) * 9, ItemUtils.createItem(Material.ARROW, 1, ComponentUtils.title("Previous Page"), ComponentUtils.line("Return to the previous page.")), event -> {
                if (event.getWhoClicked() instanceof Player player) {
                    page--;
                    this.refresh();
                    this.updatePlayerInventory(player);
                }
            });
        }

        // If more items exist than fit on the page, show the next page button.
        if ((buttonsPerPage * page) < buttonCount) {
            setItem((row * 9) - 1, ItemUtils.createItem(Material.ARROW, 1, ComponentUtils.title("Next Page"), ComponentUtils.line("Go to the next page.")), event -> {
                if (event.getWhoClicked() instanceof Player player) {
                    page++;
                    this.refresh();
                    this.updatePlayerInventory(player);
                }
            });
        }

        addAdditionalButtons();
    }
}
