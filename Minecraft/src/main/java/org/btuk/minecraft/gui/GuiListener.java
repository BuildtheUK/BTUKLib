package org.btuk.minecraft.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Gui listener, handles player interactions with a {@link Gui}.
 */
public final class GuiListener implements Listener {

    /** The Gui Manager associated with this listener. */
    private GuiManager guiManager;

    /**
     * Constructs and registers the Gui listener.
     */
    public GuiListener(GuiManager manager) {
        this.guiManager = manager;
    }

    /**
     * Unregister the Gui listener.
     *
     * @param plugin the plugin for which the listener is registered
     */
    public void register(JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Unregister the Gui listener.
     */
    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Handles inventory click events. Identifies whether the click is on a Gui of the associated Gui Manager
     * and performs necessary actions if so.
     */
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player player) {
            UUID inventoryUUID = guiManager.getOpenGuiUuidByPlayerUuid(player.getUniqueId());
            if (inventoryUUID != null) {
                e.setCancelled(true);
                Gui gui = guiManager.getGuiByUuid(inventoryUUID);
                GuiAction action = gui.getAction(e.getRawSlot());

                if (action != null) {
                    action.click(e);
                }
            }
        }
    }

    /**
     * Handles inventory click events. Identifies whether the close is on a Gui of the associated Gui Manager
     * and performs necessary actions if so.
     */
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player player) {
            // Get the uuid of the open inventory, if exists.
            UUID guiUuid = guiManager.getOpenGuiUuidByPlayer(player);

            if (guiUuid != null) {
                // Get the gui.
                Gui gui = guiManager.getGuiByUuid(guiUuid);
                if (gui != null) {
                    gui.close(player);
                }
            }
        }
    }
}
