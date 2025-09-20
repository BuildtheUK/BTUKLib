package net.bteuk.minecraft.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * {@link Gui} manager, registers en unregisters Guis as wel as keeps track of players that are currently using a Gui.
 */
public final class GuiManager {

    private final Map<UUID, Gui> registeredGuis = new HashMap<>();

    private final Map<UUID, UUID> openGuis = new HashMap<>();

    /**
     * Adds the Gui to the list of registered Guis.
     * @param gui The Gui to add to the list of registered Guis.
     */
    public void registerGui(Gui gui) {
        if (gui != null) {
            registeredGuis.put(gui.getUuid(), gui);
        }
    }

    /**
     * Removes the Gui from the list of open Guis, and removes the Gui from the list of registered Guis.
     * @param gui Gui to unregister.
     */
    public void unregisterGui(Gui gui) {
        if (gui != null) {
            unregisterGuiByUuid(gui.getUuid());
        }
    }

    /**
     * Removes the Gui from the list of open Guis, and removes the Gui from the list of registered Guis.
     * @param uuid The uuid of the Gui to unregister.
     */
    public void unregisterGuiByUuid(UUID uuid) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            openGuis.remove(player.getUniqueId(), uuid);
        }
        registeredGuis.remove(uuid);
    }

    public Gui getGuiByUuid(UUID uuid) {
        return registeredGuis.get(uuid);
    }

    public UUID getOpenGuiUuidByPlayer(Player player) {
        return getOpenGuiUuidByPlayerUuid(player.getUniqueId());
    }

    public UUID getOpenGuiUuidByPlayerUuid(UUID uuid) {
        return openGuis.get(uuid);
    }

    /**
     * Opens a Gui. Registers the Gui if it is not already registered, then opens the inventory, and adds the Gui
     * to the list of open Guis.
     * @param player The player whom to open the Gui for.
     * @param gui The Gui to open.
     */
    public void openGui(Player player, Gui gui) {
        if (!registeredGuis.containsKey(gui.getUuid()))
            registerGui(gui);
        openGui(player.getUniqueId(), gui.getUuid());
    }

    /**
     * Opens a Gui. Registers the Gui if it is not already registered, then opens the inventory, and adds the Gui
     * to the list of open Guis.
     * @param playerUuid The UUID of the player whom to open the Gui for.
     * @param gui The Gui to open.
     */
    public void openGui(UUID playerUuid, Gui gui) {
        if (!registeredGuis.containsKey(gui.getUuid()))
            registerGui(gui);
        openGui(playerUuid, gui.getUuid());
    }

    /**
     * Opens a Gui. Registers the Gui if it is not already registered, then opens the inventory, and adds the Gui
     * to the list of open Guis.
     * @param playerUuid The UUID of the player whom to open the Gui for.
     * @param guiUuid The UUID of the Gui to open.
     */
    private void openGui(UUID playerUuid, UUID guiUuid) {
        openGuis.put(playerUuid, guiUuid);
    }

    public void closeGui(Player player) {
        closeGui(player.getUniqueId());
    }

    /**
     * Removes the Player-Gui pair from the list of open Guis for the given player
     * @param playerUuid The player to remove the Open gui for
     */
    public void closeGui(UUID playerUuid) {
        openGuis.remove(playerUuid);
    }
}
