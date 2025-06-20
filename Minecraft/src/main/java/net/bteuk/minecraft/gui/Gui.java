package net.bteuk.minecraft.gui;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Instance of a Gui, a Gui is a Minecraft inventory containing items.
 * The player can interact with items to trigger actions.
 */
public abstract class Gui {

    @Getter
    private final UUID uuid;
    private Inventory inventory;
    private final Map<Integer, GuiAction> actions;
    /** The Gui manager to which this Gui belongs to */
    private final GuiManager manager;

    @Setter
    private boolean deleteOnClose = false;

    /**
     * Constructs a Gui from a given inventory size, and inventory title
     * @param manager The Gui manager which this Gui is to be managed by
     * @param inventorySize The size of the inventory. Must be a multiple of 9, no greater than 54
     * @param inventoryName The title of the inventory
     */
    public Gui(GuiManager manager, int inventorySize, Component inventoryName) {
        this(manager, Bukkit.createInventory(null, inventorySize, inventoryName));
    }

    /**
     * Constructs a Gui from an existing inventory object
     * @param manager The Gui manager which this Gui is to be managed by
     * @param inventory An inventory object to base this Gui on
     */
    public Gui(GuiManager manager, Inventory inventory) {
        this.manager = manager;
        this.inventory = inventory;
        uuid = UUID.randomUUID();
        actions = new HashMap<>();
    }

    public void setItem(int slot, ItemStack stack, net.bteuk.minecraft.gui.GuiAction action) {
        inventory.setItem(slot, stack);
        setAction(slot, action);
    }

    public void setItem(int slot, ItemStack stack) {
        setItem(slot, stack, null);
    }

    public net.bteuk.minecraft.gui.GuiAction getAction(int slot) {
        return actions.get(slot);
    }

    public void setAction(int slot, net.bteuk.minecraft.gui.GuiAction action) {
        if (action != null) {
            actions.put(slot, action);
        }
    }

    public void removeAction(int slot) {
        actions.remove(slot);
    }

    public void clear() {
        inventory.clear();
        actions.clear();
    }

    public void open(Player player) {
        player.openInventory(inventory);
        manager.openGui(player, this);
    }

    public void close(Player player) {
        // If the gui should delete on close, delete it.
        if (this.deleteOnClose) {
            delete();
        } else {
            //Remove the player from the list of open inventories.
            manager.closeGui(player);
        }
    }

    public void delete() {
        manager.unregisterGuiByUuid(this.uuid);
    }
}
